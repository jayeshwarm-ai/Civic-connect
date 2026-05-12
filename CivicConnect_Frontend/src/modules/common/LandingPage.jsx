import React, { useState } from 'react';
import { Link } from 'react-router-dom';

/* ─── Static content (kept simple & non-technical) ────────────────────── */
const SERVICES = [
  { icon: '🛣️', title: 'Roads',       desc: 'Report potholes, broken pavements, missing signs, and other road issues in your area.' },
  { icon: '💧', title: 'Water',       desc: 'Raise complaints about leaks, low pressure, contamination, or supply interruptions.' },
  { icon: '⚡', title: 'Electricity', desc: 'Notify the city about outages, faulty streetlights, or electrical hazards.' },
];

const MODULES = [
  { icon: '🔐', name: 'Identity Service',     desc: 'Securely sign in to your account. Keeps your details private and makes sure only the right people can access each part of the platform.' },
  { icon: '👤', name: 'Citizen Service',      desc: 'Create your citizen profile, share your address, and upload identity proofs so the city knows who you are when you reach out for help.' },
  { icon: '📋', name: 'Service Request',      desc: 'Report everyday civic problems and follow your complaint until it is fixed.' },
  { icon: '📊', name: 'Reporting Service',    desc: 'See how the city is doing at a glance — how many issues are coming in and how quickly they are being solved.' },
  { icon: '🔔', name: 'Notification Service', desc: 'Get a quick heads-up whenever something changes — when your complaint is picked up, worked on, or marked as done.' },
  { icon: '💬', name: 'Feedback Service',     desc: 'Tell the city how it went. Rate the service so officers know what worked and what still needs attention.' },
  { icon: '⚖️', name: 'Compliance Service',   desc: 'A neutral check on every closed complaint. Makes sure work was done properly before the case is filed away.' },
  { icon: '🔧', name: 'Resolution Service',   desc: 'The behind-the-scenes work officers do to solve your problem — the steps and progress they record.' },
];

const FAQS = [
  { q: 'What is CivicConnect?',                a: 'CivicConnect is a unified digital platform that connects citizens with city services. Register, submit requests, upload documents, and track progress — all in one place.' },
  { q: 'How do I register?',                   a: 'Click "Login / Register" in the header and choose the Register tab. Provide your personal details to create a citizen account.' },
  { q: 'What services can I request?',         a: 'You can submit service requests for Roads, Water and Electricity. Each request is tracked from submission to resolution.' },
  { q: 'How does document verification work?', a: 'After registering, upload your ID Proof and Residence Proof. A city administrator reviews them to activate your account.' },
  { q: 'How do I track my service request?',   a: 'Sign in as a citizen and open "Requests" to see status updates, the assigned officer, and full update history.' },
  { q: 'I forgot my password. What do I do?',  a: 'On the login screen, click "Forgot password?" — verify your email and phone, then set a new password.' },
];

/* ─── Page ─────────────────────────────────────────────────────────────── */
export default function LandingPage() {
  return (
    <>
      <Hero />
      <ServicesStrip />
      <ModulesSection />
      <FaqSection />
    </>
  );
}

/* ─── Sections ─────────────────────────────────────────────────────────── */

function Hero() {
  return (
    <section className="gov-hero">
      <div className="gov-hero__inner">
        <h1>Connecting citizens to civic services, transparently.</h1>
        <p>
          A single, simple way for residents to raise civic issues — roads, water, electricity —
          and follow each request from submission to resolution.
        </p>
        <div className="gov-hero__ctas">
          <Link to="/login"  className="gov-hero__cta-primary">Login / Register</Link>
          <Link to="/about" className="gov-hero__cta-secondary">Learn more</Link>
        </div>
      </div>
    </section>
  );
}

function ServicesStrip() {
  return (
    <section className="gov-section">
      <h2 className="gov-section__title">Services available to citizens</h2>
      <p  className="gov-section__lead">Pick a service to know more or sign in to raise a request.</p>
      <div className="gov-module-grid">
        {SERVICES.map((s) => (
          <article key={s.title} className="gov-module">
            <div className="gov-module__icon" aria-hidden="true">{s.icon}</div>
            <div>
              <div className="gov-module__name">{s.title}</div>
              <div className="gov-module__desc">{s.desc}</div>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}

function ModulesSection() {
  return (
    <section className="gov-section gov-section--alt" style={{ background: 'var(--gov-bg-alt)' }}>
      <h2 className="gov-section__title">What you can do on CivicConnect</h2>
      <p  className="gov-section__lead">Every part of the platform, organised into simple sections.</p>
      <div className="gov-module-grid">
        {MODULES.map((m) => (
          <article key={m.name} className="gov-module">
            <div className="gov-module__icon" aria-hidden="true">{m.icon}</div>
            <div>
              <div className="gov-module__name">{m.name}</div>
              <div className="gov-module__desc">{m.desc}</div>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}

function FaqSection() {
  const [open, setOpen] = useState(null);
  return (
    <section className="gov-section">
      <h2 className="gov-section__title">Frequently asked questions</h2>
      <div style={{ maxWidth: 800, margin: '0 auto' }}>
        {FAQS.map((f, i) => (
          <div
            key={i}
            style={{
              background: '#fff', border: '1px solid var(--gov-border)',
              borderLeft: open === i ? '4px solid var(--gov-saffron)' : '4px solid var(--gov-border)',
              borderRadius: 4, marginBottom: 10, overflow: 'hidden',
            }}
          >
            <button
              onClick={() => setOpen(open === i ? null : i)}
              style={{
                width: '100%', textAlign: 'left', padding: '16px 18px',
                background: 'transparent', border: 0, cursor: 'pointer',
                fontFamily: 'inherit', fontSize: '0.96rem', fontWeight: 700,
                color: 'var(--gov-text)', display: 'flex',
                justifyContent: 'space-between', alignItems: 'center',
              }}
            >
              {f.q}
              <span style={{ color: 'var(--gov-text-muted)' }}>{open === i ? '−' : '+'}</span>
            </button>
            {open === i && (
              <div style={{ padding: '0 18px 18px', color: 'var(--gov-text-muted)', lineHeight: 1.6 }}>
                {f.a}
              </div>
            )}
          </div>
        ))}
      </div>
    </section>
  );
}
