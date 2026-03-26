import { useNavigate } from 'react-router-dom'
import './Home.css'

function Home() {
  const navigate = useNavigate()

  return (
    <div className="home">
      <header className="home-header">
        <h1 className="home-title">AI Agent</h1>
        <p className="home-subtitle">选择你想要使用的 AI 应用</p>
      </header>

      <div className="apps-grid">
        <div className="app-card" onClick={() => navigate('/code-guide')}>
          <div className="app-icon">☕</div>
          <h2 className="app-name">AI Java 编程导师</h2>
          <p className="app-desc">智能 Java 编程助手，<br />为你解答代码问题</p>
        </div>

        <div className="app-card" onClick={() => navigate('/agent1')}>
          <div className="app-icon">🤖</div>
          <h2 className="app-name">AI Agent</h2>
          <p className="app-desc">智能 Agent 应用，<br />帮助你完成复杂任务</p>
        </div>
      </div>
    </div>
  )
}

export default Home