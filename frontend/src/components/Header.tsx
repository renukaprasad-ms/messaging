import { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router";
import { authService } from "../service/authService";
import { logout } from "../store/auth/authSlice";
import type { RootState } from "../store/store";

const Header = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const user = useSelector((state: RootState) => state.auth.user);
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const handleLogout = async () => {
    setIsLoggingOut(true);

    try {
      await authService.logout();
    } finally {
      dispatch(logout());
      setIsLoggingOut(false);
      navigate("/login", { replace: true });
    }
  };

  return (
    <header className="border-b border-slate-200 bg-white px-6 py-4">
      <div className="mx-auto flex max-w-6xl items-center justify-between gap-4">
        <div>
          <p className="text-lg font-bold text-slate-950">Messaging</p>
          {user ? <p className="text-xs text-slate-500">{user.email}</p> : null}
        </div>
        <button type="button" onClick={handleLogout} disabled={isLoggingOut} className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-semibold text-slate-700 transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:text-slate-400">
          {isLoggingOut ? "Signing out..." : "Logout"}
        </button>
      </div>
    </header>
  );
};

export default Header;
