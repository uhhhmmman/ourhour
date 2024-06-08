import React from "react";
import newLogo from "../assets/new_logo.png"; // 새로운 로고 이미지 경로
import { Link, useNavigate } from "react-router-dom";

function TopNav() {
  const navigate = useNavigate();

  // Video Chat 클릭 시 동작할 함수
  const handleVideoChat = () => {
    navigate("/friendMain");
  };

  //mypage 클릭시 동작할 함수
  const handleMyPage = () => {
    navigate("/mypage");
  }

  return (
    <header className="header">
      <div className="logo">
        <button className="logo-button">
          <a href="/" ><img src={newLogo} alt="New Logo" /></a>
        </button>
      </div>
      <nav className="nav">
        {/* Video Chat 버튼 */}
        <button className="nav-button video-chat-btn" onClick={handleVideoChat}>Video Chat</button>
        <Link to="/cr" className="action-link">방만들기</Link>
        <Link to="/roomList" className="action-link">방목록</Link>
        <Link to="/about" className="action-link">About</Link>
        <button className="action-button" onClick={handleMyPage}>My Page</button>
        <div className="login-options">
          <Link to="/login" className="login-btn">Login</Link>
        </div>
      </nav>
    </header>
  );
}

export default TopNav;
