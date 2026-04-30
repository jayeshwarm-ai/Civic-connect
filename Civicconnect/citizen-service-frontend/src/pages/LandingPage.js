import React, { useState } from 'react';
import { Link } from 'react-router-dom';

const faqs = [
  { q: 'What is CivicConnect?', a: 'CivicConnect is a unified digital platform that connects citizens with city services. Register, submit service requests, upload documents, and track progress — all in one place.' },
  { q: 'How do I register?', a: 'Click "Register" and choose your role. Citizens need to provide personal details and upload identity documents. Staff accounts (officers, admins) are created with immediate access.' },
  { q: 'What services can I request?', a: 'You can submit service requests for Road maintenance, Water supply issues, and Electricity problems. Each request is tracked from submission to resolution.' },
  { q: 'How does document verification work?', a: 'After registering as a citizen, upload your ID Proof and Residence Proof. A city administrator will review and verify your documents to activate your account.' },
  { q: 'How do I track my service request?', a: 'Login as a citizen, go to "My Requests" to see all your submissions with real-time status updates, assigned officer details, and full update history.' },
  { q: 'I forgot my password. What do I do?', a: 'Click "Reset Password" on the navigation bar or login page. Verify your identity using your registered email and phone number, then set a new password.' },
];

export default function LandingPage() {
  const [openFaq, setOpenFaq] = useState(null);

  return (
    <div style={{ margin: '-32px -24px -60px' }}>
      {/* ══ HERO ══ */}
      <section style={{
        background: 'linear-gradient(135deg, #0f172a 0%, #1e3a5f 40%, #312e81 70%, #4f46e5 100%)',
        color: 'white', padding: '80px 24px 90px', textAlign: 'center', position: 'relative', overflow: 'hidden'
      }}>
        <div style={{ position: 'absolute', top: -80, right: -80, width: 300, height: 300, borderRadius: '50%', background: 'rgba(99,102,241,0.15)' }} />
        <div style={{ position: 'absolute', bottom: -60, left: -60, width: 250, height: 250, borderRadius: '50%', background: 'rgba(6,182,212,0.1)' }} />
        <div style={{ maxWidth: 800, margin: '0 auto', position: 'relative', zIndex: 1 }}>
          <div style={{
            width: 72, height: 72, borderRadius: 20, margin: '0 auto 24px',
            background: 'linear-gradient(135deg, #818cf8, #06b6d4)',
            display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '2.2rem',
            boxShadow: '0 8px 30px rgba(79,70,229,0.4)'
          }}>🏛️</div>
          <h1 style={{ fontSize: '3rem', fontWeight: 900, letterSpacing: -1, marginBottom: 16, lineHeight: 1.1 }}>
            Welcome to <span style={{ color: '#67e8f9' }}>CivicConnect</span>
          </h1>
          <p style={{ fontSize: '1.25rem', opacity: 0.85, maxWidth: 600, margin: '0 auto 36px', lineHeight: 1.6 }}>
            Empowering citizens. Transforming governance.<br />
            Your one-stop digital platform for civic services.
          </p>
          <div style={{ display: 'flex', gap: 16, justifyContent: 'center', flexWrap: 'wrap' }}>
            <Link to="/register" className="btn btn-primary" style={{ padding: '14px 36px', fontSize: '1rem' }}>🚀 Get Started</Link>
            <Link to="/login" className="btn" style={{ padding: '14px 36px', fontSize: '1rem', background: 'rgba(255,255,255,0.12)', color: 'white', border: '2px solid rgba(255,255,255,0.25)' }}>🔑 Sign In</Link>
            <a href="#about" className="btn" style={{ padding: '14px 36px', fontSize: '1rem', background: 'transparent', color: 'rgba(255,255,255,0.7)', border: '2px solid rgba(255,255,255,0.15)' }}>Learn More ↓</a>
          </div>
        </div>
      </section>

      {/* ══ ABOUT ══ */}
      <section id="about" style={{ maxWidth: 1100, margin: '0 auto', padding: '70px 24px' }}>
        <div style={{ textAlign: 'center', marginBottom: 48 }}>
          <h2 style={{ fontSize: '2rem', fontWeight: 800, color: '#0f172a', marginBottom: 12 }}>About CivicConnect</h2>
          <p style={{ color: '#64748b', fontSize: '1.05rem', maxWidth: 700, margin: '0 auto', lineHeight: 1.7 }}>
            CivicConnect bridges the gap between citizens and city administration. Our microservices-based platform enables seamless citizen registration,
            document verification, service request management, and real-time tracking — making governance transparent, efficient, and accessible to everyone.
          </p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: 24 }}>
          {[
            { icon: '👤', title: 'Citizen Service', desc: 'Register as a citizen, manage your profile, upload identity & residence documents, and get verified by administrators.', color: '#eef2ff' },
            { icon: '📋', title: 'Service Requests', desc: 'Submit requests for Road, Water, and Electricity issues. Track status from submission to resolution in real-time.', color: '#f0fdf4' },
            { icon: '🔐', title: 'Identity & Access', desc: 'Secure authentication with JWT, role-based access control for Citizens, Officers, Department Heads, and Administrators.', color: '#fef3c7' },
          ].map((s, i) => (
            <div key={i} style={{
              background: 'white', borderRadius: 20, padding: 32, boxShadow: '0 4px 6px -1px rgba(0,0,0,0.07)',
              border: '1px solid #f1f5f9', transition: 'all 0.25s', cursor: 'default'
            }}
              onMouseEnter={e => { e.currentTarget.style.transform = 'translateY(-4px)'; e.currentTarget.style.boxShadow = '0 12px 30px rgba(0,0,0,0.1)'; }}
              onMouseLeave={e => { e.currentTarget.style.transform = 'translateY(0)'; e.currentTarget.style.boxShadow = '0 4px 6px -1px rgba(0,0,0,0.07)'; }}
            >
              <div style={{ width: 56, height: 56, borderRadius: 16, background: s.color, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.6rem', marginBottom: 20 }}>{s.icon}</div>
              <h3 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: 10, color: '#0f172a' }}>{s.title}</h3>
              <p style={{ color: '#64748b', lineHeight: 1.7, fontSize: '0.92rem' }}>{s.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* ══ HOW IT WORKS ══ */}
      <section style={{ background: '#f8fafc', padding: '70px 24px' }}>
        <div style={{ maxWidth: 1100, margin: '0 auto' }}>
          <h2 style={{ fontSize: '2rem', fontWeight: 800, textAlign: 'center', marginBottom: 48, color: '#0f172a' }}>How It Works</h2>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 32 }}>
            {[
              { step: '1', icon: '📝', title: 'Register', desc: 'Create your account and choose your role.' },
              { step: '2', icon: '📄', title: 'Upload Documents', desc: 'Submit ID Proof and Residence Proof for verification.' },
              { step: '3', icon: '✅', title: 'Get Verified', desc: 'Administrator reviews and activates your account.' },
              { step: '4', icon: '📋', title: 'Submit Requests', desc: 'Report road, water, or electricity issues.' },
              { step: '5', icon: '🔄', title: 'Track Progress', desc: 'Monitor your request through every stage.' },
            ].map((s, i) => (
              <div key={i} style={{ textAlign: 'center' }}>
                <div style={{
                  width: 64, height: 64, borderRadius: '50%', margin: '0 auto 16px',
                  background: 'linear-gradient(135deg, #4f46e5, #06b6d4)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: '1.5rem', color: 'white', fontWeight: 800,
                  boxShadow: '0 4px 14px rgba(79,70,229,0.3)'
                }}>{s.icon}</div>
                <div style={{ fontSize: '0.7rem', fontWeight: 800, color: '#94a3b8', textTransform: 'uppercase', letterSpacing: 1, marginBottom: 6 }}>Step {s.step}</div>
                <h4 style={{ fontWeight: 700, marginBottom: 6, color: '#0f172a' }}>{s.title}</h4>
                <p style={{ fontSize: '0.88rem', color: '#64748b', lineHeight: 1.6 }}>{s.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* ══ MODULES ══ */}
      <section style={{ maxWidth: 1100, margin: '0 auto', padding: '70px 24px' }}>
        <h2 style={{ fontSize: '2rem', fontWeight: 800, textAlign: 'center', marginBottom: 16, color: '#0f172a' }}>Platform Modules</h2>
        <p style={{ textAlign: 'center', color: '#64748b', marginBottom: 48, maxWidth: 600, margin: '0 auto 48px' }}>
          CivicConnect is powered by a robust microservices architecture with specialized modules.
        </p>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: 20 }}>
          {[
            { icon: '🔐', name: 'Identity Service', desc: 'User authentication, JWT tokens, role management, audit logging', port: '8081' },
            { icon: '👤', name: 'Citizen Service', desc: 'Registration, profile management, document upload & verification', port: '8082' },
            { icon: '📋', name: 'Service Request', desc: 'Submit, assign, track & resolve civic service requests', port: '8083' },
            { icon: '📊', name: 'Reporting Service', desc: 'Analytics, dashboards, and performance metrics', port: '8084' },
            { icon: '🔔', name: 'Notification Service', desc: 'Email/SMS alerts for status changes and updates', port: '8085' },
            { icon: '💬', name: 'Feedback Service', desc: 'Citizen feedback and satisfaction surveys', port: '8086' },
            { icon: '⚖️', name: 'Compliance Service', desc: 'Regulatory compliance tracking and policy enforcement', port: '8087' },
            { icon: '🔧', name: 'Resolution Service', desc: 'Escalation management and resolution workflows', port: '8088' },
          ].map((m, i) => (
            <div key={i} style={{
              background: 'white', borderRadius: 14, padding: '22px 24px',
              border: '1px solid #e2e8f0', display: 'flex', alignItems: 'flex-start', gap: 16,
              transition: 'all 0.2s'
            }}
              onMouseEnter={e => { e.currentTarget.style.borderColor = '#818cf8'; e.currentTarget.style.boxShadow = '0 4px 12px rgba(79,70,229,0.1)'; }}
              onMouseLeave={e => { e.currentTarget.style.borderColor = '#e2e8f0'; e.currentTarget.style.boxShadow = 'none'; }}
            >
              <div style={{ fontSize: '1.5rem', flexShrink: 0, marginTop: 2 }}>{m.icon}</div>
              <div>
                <div style={{ fontWeight: 700, fontSize: '0.95rem', color: '#0f172a', marginBottom: 4 }}>{m.name}</div>
                <div style={{ fontSize: '0.82rem', color: '#64748b', lineHeight: 1.5 }}>{m.desc}</div>
                <div style={{ fontSize: '0.7rem', color: '#94a3b8', marginTop: 6, fontWeight: 700 }}>Port: {m.port}</div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ══ FAQ ══ */}
      <section style={{ background: '#f8fafc', padding: '70px 24px' }}>
        <div style={{ maxWidth: 800, margin: '0 auto' }}>
          <h2 style={{ fontSize: '2rem', fontWeight: 800, textAlign: 'center', marginBottom: 48, color: '#0f172a' }}>❓ Frequently Asked Questions</h2>
          {faqs.map((f, i) => (
            <div key={i} style={{
              background: 'white', borderRadius: 14, marginBottom: 12,
              border: `1px solid ${openFaq === i ? '#818cf8' : '#e2e8f0'}`,
              overflow: 'hidden', transition: 'all 0.2s'
            }}>
              <button onClick={() => setOpenFaq(openFaq === i ? null : i)} style={{
                width: '100%', padding: '20px 24px', border: 'none', background: openFaq === i ? '#eef2ff' : '#fafafa',
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                cursor: 'pointer', fontFamily: 'inherit', fontSize: '0.95rem', fontWeight: 700, color: '#0f172a', textAlign: 'left'
              }}>
                <span>{f.q}</span>
                <span style={{
                  width: 28, height: 28, borderRadius: '50%', flexShrink: 0, marginLeft: 12,
                  background: openFaq === i ? '#4f46e5' : '#e2e8f0',
                  color: openFaq === i ? 'white' : '#64748b',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontSize: '1rem', transition: 'all 0.2s', transform: openFaq === i ? 'rotate(45deg)' : 'none'
                }}>+</span>
              </button>
              {openFaq === i && (
                <div style={{ padding: '16px 24px 20px', color: '#475569', fontSize: '0.92rem', lineHeight: 1.7, borderTop: '1px solid #e2e8f0' }}>
                  {f.a}
                </div>
              )}
            </div>
          ))}
        </div>
      </section>

      {/* ══ CONTACT ══ */}
      <section id="contact" style={{ maxWidth: 1100, margin: '0 auto', padding: '70px 24px' }}>
        <h2 style={{ fontSize: '2rem', fontWeight: 800, textAlign: 'center', marginBottom: 48, color: '#0f172a' }}>📞 Contact Us</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: 24 }}>
          {[
            { icon: '📧', label: 'Email', value: 'support@civicconnect.gov' },
            { icon: '📞', label: 'Phone', value: '1800-CIVIC-HELP' },
            { icon: '🕐', label: 'Hours', value: 'Mon–Fri, 9 AM – 5 PM' },
            { icon: '📍', label: 'Office', value: 'City Municipal Office, Main Road' },
          ].map((c, i) => (
            <div key={i} style={{
              background: 'white', borderRadius: 14, padding: 28, textAlign: 'center',
              border: '1px solid #e2e8f0', boxShadow: '0 2px 4px rgba(0,0,0,0.04)'
            }}>
              <div style={{ fontSize: '2rem', marginBottom: 12 }}>{c.icon}</div>
              <div style={{ fontSize: '0.72rem', fontWeight: 800, color: '#94a3b8', textTransform: 'uppercase', letterSpacing: 1, marginBottom: 6 }}>{c.label}</div>
              <div style={{ fontWeight: 700, color: '#0f172a', fontSize: '1rem' }}>{c.value}</div>
            </div>
          ))}
        </div>
      </section>

      {/* ══ FOOTER ══ */}
      <footer style={{
        background: 'linear-gradient(135deg, #0f172a, #1e293b)', color: '#94a3b8',
        padding: '36px 24px', textAlign: 'center'
      }}>
        <div style={{ fontSize: '1.2rem', fontWeight: 800, color: 'white', marginBottom: 8 }}>
          🏛️ Civic<span style={{ color: '#67e8f9' }}>Connect</span>
        </div>
        <p style={{ fontSize: '0.85rem', marginBottom: 16 }}>Empowering citizens. Transforming governance.</p>
        <div style={{ display: 'flex', gap: 20, justifyContent: 'center', marginBottom: 16, flexWrap: 'wrap' }}>
          <a href="#about" style={{ color: '#94a3b8', textDecoration: 'none', fontSize: '0.85rem' }}>About</a>
          <a href="#contact" style={{ color: '#94a3b8', textDecoration: 'none', fontSize: '0.85rem' }}>Contact</a>
          <Link to="/register" style={{ color: '#94a3b8', textDecoration: 'none', fontSize: '0.85rem' }}>Register</Link>
          <Link to="/login" style={{ color: '#94a3b8', textDecoration: 'none', fontSize: '0.85rem' }}>Login</Link>
        </div>
        <p style={{ fontSize: '0.78rem', color: '#475569' }}>© 2026 CivicConnect. All rights reserved.</p>
      </footer>
    </div>
  );
}

