import React from 'react';
import { Link } from 'react-router-dom';
import { PageHeader } from '../../components/ui';

/**
 * AboutPage — public information page about CivicConnect.
 * Pure content, no API calls, no auth required.
 */
export default function AboutPage() {
  return (
    <div style={{ maxWidth: 1100, margin: '0 auto', padding: '24px 24px 60px' }}>
      <PageHeader
        title="About CivicConnect"
        subtitle="A digital bridge between citizens and the city — open, fast, and accountable."
        icon="🏛️"
      />

      <section className="gov-about__intro">
        <h2>Our mission</h2>
        <p>
          CivicConnect helps citizens raise everyday problems with their city — a broken streetlight,
          a leaking pipe, a damaged road — and follow their complaint until it is fixed. The platform
          gives elected representatives, administrators, and field officers a clear, shared view of
          what residents need, who is responsible, and how quickly things are getting done.
        </p>
      </section>

      <section className="gov-about__cols">
        <div className="gov-about__col">
          <h3>What CivicConnect does</h3>
          <ul>
            <li>Lets citizens report civic issues in minutes, from anywhere.</li>
            <li>Routes each request to the right department and officer.</li>
            <li>Keeps citizens informed at every step of the process.</li>
            <li>Lets citizens close the loop with feedback after a fix.</li>
            <li>Provides administrators with reports and dashboards.</li>
            <li>Maintains an independent compliance check on closed cases.</li>
          </ul>
        </div>

        <div className="gov-about__col">
          <h3>Who it serves</h3>
          <ul>
            <li><strong>Citizens</strong> — register, raise requests, share feedback.</li>
            <li><strong>Service officers</strong> — work assigned cases and record resolutions.</li>
            <li><strong>Department heads</strong> — assign work and monitor outcomes.</li>
            <li><strong>City administrators</strong> — oversee everything end-to-end.</li>
            <li><strong>Compliance officers</strong> — audit closed cases for quality and standards.</li>
          </ul>
        </div>

        <div className="gov-about__col">
          <h3>Our principles</h3>
          <ul>
            <li><strong>Transparency</strong> — every step of every request is visible.</li>
            <li><strong>Accountability</strong> — work is tracked to a named officer.</li>
            <li><strong>Speed</strong> — the right person hears about the issue first.</li>
            <li><strong>Privacy</strong> — citizen data is access-controlled by role.</li>
            <li><strong>Inclusion</strong> — designed to be simple for everyone.</li>
          </ul>
        </div>
      </section>

      <section className="gov-about__intro" style={{ marginTop: 24, borderLeftColor: 'var(--gov-green)' }}>
        <h2>Get started</h2>
        <p>
          Citizens can <Link to="/login">sign in</Link> to an existing account or{' '}
          <Link to="/register">register</Link> to begin raising requests. Officials are onboarded
          by the city administrator.
        </p>
      </section>
    </div>
  );
}
