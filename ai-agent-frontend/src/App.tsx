import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Home from './pages/Home'
import CodeGuideChat from './pages/CodeGuideChat'
import Agent1Chat from './pages/Agent1Chat'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/code-guide" element={<CodeGuideChat />} />
        <Route path="/agent1" element={<Agent1Chat />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App