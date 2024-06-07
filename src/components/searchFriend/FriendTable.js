import React from "react";

function FriendTable() {
  return (
    <>
      <div className="py-4 flex justify-center ml-100">
        <table className="min-w-full text-md bg-white rounded mb-4">
          <tbody>
            <tr className="border-b">
              <th className="text-center p-3 px-5">Name</th>
              <th className="text-center p-3 px-5">Email</th>
              <th />
            </tr>
            <tr className="border-b hover:bg-orange-100 bg-gray-100">
              <td className="p-3 px-5">
                <input
                  type="text"
                  defaultValue="user.name"
                  className="bg-transparent"
                />
              </td>
              <td className="p-3 px-5">
                <input
                  type="text"
                  defaultValue="user.email"
                  className="bg-transparent"
                />
              </td>
              <td className="p-3 px-5 flex justify-end">
                <button
                  type="button"
                  className="text-xl bg-red-500 hover:bg-red-700 text-white py-1 px-2 rounded focus:outline-none focus:shadow-outline"
                >
                  삭제
                </button>
              </td>
            </tr>

          </tbody>
        </table>
      </div>
    </>
  )
}

export default FriendTable;