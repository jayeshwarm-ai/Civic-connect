import React from 'react';
import { Link } from 'react-router-dom';

/**
 * Footer — global footer shown on every page.
 * Pure presentational, no data fetching.
 */
function Footer() {
  const year = new Date().getFullYear();
  return (
    <footer className="gov-footer">
      <div className="gov-footer__inner">
        <div className="gov-footer__cols">
          <div>
            <h4>About CivicConnect</h4>
            <p>
              CivicConnect is a digital citizen-services platform for raising and tracking
              everyday civic issues. Built for transparency, accountability, and speed.
            </p>
          </div>
          <div>
            <h4>Quick Links</h4>
            <ul>
              <li><Link to="/">Home</Link></li>
              <li><Link to="/about">About</Link></li>
              <li><Link to="/login">Login / Register</Link></li>
            </ul>
          </div>
          <div>
            <h4>Help</h4>
            <ul>
              <li>Email: support@civicconnect.gov</li>
              <li>Helpline: 1800-XXX-XXXX</li>
              <li>Hours: Mon–Fri, 9 AM – 6 PM</li>
            </ul>
          </div>
        </div>
        <div className="gov-footer__bottom">
          <span>© {year} CivicConnect. All rights reserved.</span>
          <span>Best viewed in latest Chrome, Firefox, Edge.</span>
        </div>
      </div>
    </footer>
  );
}

export default Footer;
