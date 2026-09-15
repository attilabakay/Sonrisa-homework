// The logged-in identity strip. Login/registration live in AuthScreen — this only ever
// renders once currentUser exists.
export default function UserBar({ currentUser, onLogout }) {
  return (
    <div className="user-bar">
      <span>Logged in as {currentUser.email}</span>
      <span className={`badge ${currentUser.admin ? 'badge-admin' : ''}`}>{currentUser.admin ? 'admin' : 'user'}</span>
      <button type="button" onClick={onLogout}>
        Log out
      </button>
    </div>
  )
}
