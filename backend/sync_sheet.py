#!/usr/bin/env python3
"""
Đồng bộ file Integration_Test_Cases_Movie_Ticket_Booking.xlsx với bộ JUnit.

Cách dùng (sau khi đã chạy `./mvnw test` trong thư mục backend):

    python3 sync_sheet.py \
        --xlsx  Integration_Test_Cases_Movie_Ticket_Booking.xlsx \
        --tests backend/src/test/java/com/movieticketbooking/integration \
        --reports backend/target/surefire-reports \
        --out   Integration_Test_Cases_Movie_Ticket_Booking_result.xlsx

Script làm 3 việc:
 1. Quét source test, lấy mã IT-xxx trong @DisplayName -> tên hàm JUnit (cột K).
 2. Ghi cột L: dự đoán từ phân tích code (để đối chiếu, KHÔNG phải kết quả chạy).
 3. Nếu có báo cáo Surefire: điền cột H (Actual Result) và I (Status) theo kết quả thật.
    Không có báo cáo cho ID nào thì giữ nguyên "Not run".

Trạng thái: passed -> Pass, failure/error -> Fail, skipped -> Not run.
"""
import argparse
import glob
import os
import re
import xml.etree.ElementTree as ET

from openpyxl import load_workbook
from openpyxl.styles import Alignment, Font

SHEET = "Integration Test Cases"
ID_RE = re.compile(r"@DisplayName\(\s*\"(IT-[A-Z]+-\d{2})")
METHOD_RE = re.compile(r"\s*void\s+(\w+)\s*\(")

# Dự đoán từ phân tích source (không phải kết quả thực tế)
PREDICTIONS = {
    "IT-AUTH-09": "Đã kiểm tra code: /tickets/history trả 200 với mảng rỗng khi user chưa có vé.",
    "IT-AUTH-12": "Dự kiến FAIL: refreshToken là @Transactional và ném RuntimeException sau setRevoked(true), nên thay đổi bị rollback.",
    "IT-TICKET-03": "Code không cố định 401 hay 403; test chấp nhận cả hai.",
    "IT-TICKET-10": "Phụ thuộc MySQL InnoDB và PESSIMISTIC_WRITE (findByIdWithLock). Hai request chạy song song bằng 2 thread.",
    "IT-TICKET-22": "USER bị Spring Security chặn (403) trước controller nên không có message tiếng Việt như sheet; test chỉ kiểm tra status 403.",
    "IT-PAY-04": "Code không cố định 401 hay 403; test chấp nhận cả hai.",
    "IT-PAY-09": "Dự kiến PASS: PaymentService và TicketService chung một transaction nên rollback đúng.",
    "IT-CLEAN-04": "Dự kiến FAIL: TicketCleanupServiceImpl không set cancelledAt trên TicketSeat, nên createTicket vẫn báo 409 cho user B.",
    "IT-SEC-01": "Test mất khoảng 60 giây vì phải chờ hết cửa sổ rate limit.",
}


def scan_test_methods(tests_dir):
    """Trả về dict: ID -> (tên class, tên hàm)."""
    mapping = {}
    for path in glob.glob(os.path.join(tests_dir, "*Test.java")):
        cls = os.path.splitext(os.path.basename(path))[0]
        lines = open(path, encoding="utf-8").read().splitlines()
        for i, line in enumerate(lines):
            m = ID_RE.search(line)
            if not m:
                continue
            test_id = m.group(1)
            for j in range(i + 1, min(i + 6, len(lines))):
                mm = METHOD_RE.match(lines[j])
                if mm:
                    mapping[test_id] = (cls, mm.group(1))
                    break
    return mapping


def read_surefire(reports_dir, mapping):
    """Trả về dict: ID -> (status, message) từ các file TEST-*.xml."""
    results = {}
    if not reports_dir or not os.path.isdir(reports_dir):
        return results
    method_to_id = {(c, m): tid for tid, (c, m) in mapping.items()}
    for xml_path in glob.glob(os.path.join(reports_dir, "TEST-*.xml")):
        root = ET.parse(xml_path).getroot()
        for tc in root.iter("testcase"):
            cls = (tc.get("classname") or "").split(".")[-1]
            name = tc.get("name") or ""
            # Ưu tiên khớp theo ID có trong display name, sau đó theo tên hàm
            id_match = re.search(r"IT-[A-Z]+-\d{2}", name)
            test_id = id_match.group(0) if id_match else None
            if test_id is None:
                base = name.split("(")[0].strip()
                test_id = method_to_id.get((cls, base))
            if test_id is None:
                continue

            failure = tc.find("failure")
            error = tc.find("error")
            skipped = tc.find("skipped")
            if failure is not None:
                results[test_id] = ("Fail", "Thực tế: " + (failure.get("message") or "assertion failed")[:300])
            elif error is not None:
                results[test_id] = ("Fail", "Lỗi: " + (error.get("message") or error.get("type") or "error")[:300])
            elif skipped is not None:
                results[test_id] = ("Not run", "Bỏ qua khi chạy")
            else:
                results[test_id] = ("Pass", "Khớp kỳ vọng (JUnit: passed)")
    return results


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--xlsx", required=True)
    ap.add_argument("--tests", required=True)
    ap.add_argument("--reports", default=None)
    ap.add_argument("--out", required=True)
    args = ap.parse_args()

    mapping = scan_test_methods(args.tests)
    results = read_surefire(args.reports, mapping)

    wb = load_workbook(args.xlsx)
    ws = wb[SHEET]
    header_font = Font(bold=True)
    wrap = Alignment(wrap_text=True, vertical="top")

    # Header cho 2 cột mới
    for col, title in (("K", "Test method (JUnit)"), ("L", "Dự đoán từ phân tích code")):
        ws[f"{col}1"] = title
        ws[f"{col}1"].font = header_font
        ws.column_dimensions[col].width = 42 if col == "L" else 36

    filled, missing = 0, []
    for row in range(2, ws.max_row + 1):
        test_id = ws[f"A{row}"].value
        if not test_id:
            continue
        if test_id in mapping:
            cls, method = mapping[test_id]
            ws[f"K{row}"] = f"{cls}#{method}"
        else:
            missing.append(test_id)
        if test_id in PREDICTIONS:
            ws[f"L{row}"] = PREDICTIONS[test_id]
        ws[f"K{row}"].alignment = wrap
        ws[f"L{row}"].alignment = wrap

        if test_id in results:
            status, actual = results[test_id]
            ws[f"I{row}"] = status
            ws[f"H{row}"] = actual
            ws[f"H{row}"].alignment = wrap
            filled += 1
        # Không có báo cáo: giữ nguyên Status; ô đang trống thì đặt "Not run" để Summary đếm đủ
        if not ws[f"I{row}"].value:
            ws[f"I{row}"] = "Not run"

    wb.save(args.out)

    print(f"Đã ghi: {args.out}")
    print(f"Test có trong source: {len(mapping)}")
    print(f"Kết quả Surefire đã điền: {filled}")
    if missing:
        print("Cảnh báo, ID trong sheet chưa có test tương ứng:", ", ".join(missing))
    if not results:
        print("Chưa có báo cáo Surefire nên Actual Result và Status vẫn để nguyên (Not run).")


if __name__ == "__main__":
    main()
