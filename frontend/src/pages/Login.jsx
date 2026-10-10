import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

export default function Login() {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");
  const [isError, setIsError] = useState(false);
  const [loading, setLoading] = useState(false);

  async function handleLogin(event) {
    event.preventDefault();
    setMessage("");
    setLoading(true);

    try {
      const response = await fetch("http://localhost:8085/api/auth/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || "Login failed.");
      }

      setIsError(false);
      setMessage("Login successful! Redirecting...");

      setTimeout(() => {
        navigate("/home", {
          state: {
            name: data.name,
            email: data.email,
          },
        });
      }, 1000);
    } catch (error) {
      setIsError(true);
      setMessage(
        error.message === "Failed to fetch"
          ? "Cannot connect to the backend. Check that Spring Boot is running on port 8085."
          : error.message,
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-card" onSubmit={handleLogin}>
        <h1>Welcome Back</h1>
        <p className="subtitle">Login to your account</p>

        <label>Email Address</label>
        <input
          type="email"
          placeholder="Enter your email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          required
        />

        <label>Password</label>
        <input
          type="password"
          placeholder="Enter your password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          required
        />

        {message && (
          <p role="status" style={{ color: isError ? "red" : "green" }}>
            {message}
          </p>
        )}

        <button type="submit" disabled={loading}>
          {loading ? "Logging in..." : "Login"}
        </button>

        <p className="switch-link">
          Don't have an account? <Link to="/signup">Sign Up</Link>
        </p>
      </form>
    </div>
  );
}
