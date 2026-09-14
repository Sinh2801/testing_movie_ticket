import React from 'react';

export default function AdminUsers() {
  return (
    <div className="space-y-6">
      <div className="border-b border-slate-800 pb-4">
        <h1 className="text-2xl font-extrabold text-white">Quản Lý Tài Khoản</h1>
        <p className="text-xs text-slate-400">Danh sách tài khoản khách hàng & quản trị viên</p>
      </div>

      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
        <table className="w-full text-left text-sm text-slate-300">
          <thead className="bg-slate-800 text-slate-200 text-xs uppercase font-bold">
            <tr>
              <th className="p-4">ID</th>
              <th className="p-4">Họ và Tên</th>
              <th className="p-4">Email</th>
              <th className="p-4">Vai Trò</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800">
            <tr className="hover:bg-slate-800/50">
              <td className="p-4 font-mono font-bold text-red-400">#1</td>
              <td className="p-4 font-semibold text-white">System Admin</td>
              <td className="p-4">admin@cinema.com</td>
              <td className="p-4"><span className="bg-red-950 text-red-400 border border-red-800 px-2 py-0.5 rounded text-xs font-bold">ADMIN</span></td>
            </tr>
            <tr className="hover:bg-slate-800/50">
              <td className="p-4 font-mono font-bold text-red-400">#2</td>
              <td className="p-4 font-semibold text-white">Nguyễn Văn A</td>
              <td className="p-4">nguyenvana@gmail.com</td>
              <td className="p-4"><span className="bg-slate-800 text-slate-300 border border-slate-700 px-2 py-0.5 rounded text-xs font-bold">USER</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  );
}
