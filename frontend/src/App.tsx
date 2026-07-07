import { Routes, Route, Navigate } from 'react-router-dom'
import StartPage from './pages/StartPage'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import InterestSelectionPage from './pages/InterestSelectionPage'
import HomePage from './pages/HomePage'
import MusicDetailPage from './pages/MusicDetailPage'
import UploadMusicPage from './pages/UploadMusicPage'
import AdminHomePage from './admin/AdminHomePage'
import AdminPrizesPage from './admin/AdminPrizesPage'
import AdminRulesPage from './admin/AdminRulesPage'
import AdminMusicPage from './admin/AdminMusicPage'
import AdminRecordsPage from './admin/AdminRecordsPage'
import { ToastHost } from './ui/toast'
import SessionExpiredOverlay from './components/SessionExpiredOverlay'

export default function App() {
  return (
    <>
      <Routes>
        {/* Music Map for regular users */}
        <Route path="/" element={<StartPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/interests" element={<InterestSelectionPage />} />
        <Route path="/home" element={<HomePage />} />
        <Route path="/music/:id" element={<MusicDetailPage />} />
        <Route path="/upload" element={<UploadMusicPage />} />

        {/* Reward admin, entered from the account menu after confirmation */}
        <Route path="/admin" element={<AdminHomePage />} />
        <Route path="/admin/prizes" element={<AdminPrizesPage />} />
        <Route path="/admin/rules" element={<AdminRulesPage />} />
        <Route path="/admin/music" element={<AdminMusicPage />} />
        <Route path="/admin/records" element={<AdminRecordsPage />} />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      <ToastHost />
      <SessionExpiredOverlay />
    </>
  )
}
