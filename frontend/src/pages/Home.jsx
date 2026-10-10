import { Link, useLocation } from "react-router-dom";

export default function Home() {
  const location = useLocation();

  const user = location.state;

  return (
    <div className="home-page">
      <nav className="navbar">
        <h2>MyWebsite</h2>

        <div>
          {user ? (
            <>
              <span>Hi, {user.name}</span>
              <Link
                to="/"
                className="nav-button"
                onClick={() => window.location.reload()}
              >
                Logout
              </Link>
            </>
          ) : (
            <>
              <Link to="/login">Login</Link>
              <Link to="/signup" className="nav-button">
                Sign Up
              </Link>
            </>
          )}
        </div>
      </nav>

      <main className="hero">
        {user ? (
          <>
            <p className="eyebrow">LOGIN SUCCESSFUL</p>
            <h1>Welcome, {user.name}!</h1>
            <p>You are logged in with {user.email}.</p>
            <p>Your journey with MyWebsite starts here.</p>
          </>
        ) : (
          <>
            <p className="eyebrow">WELCOME TO MY WEBSITE</p>
            <h1>Your journey starts here.</h1>
            <p>A modern website built with React, Spring Boot, and MySQL.</p>
            <Link to="/signup" className="primary-link">
              Get Started
            </Link>
          </>
        )}
      </main>
    </div>
  );
}
