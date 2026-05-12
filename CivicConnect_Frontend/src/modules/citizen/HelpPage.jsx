import React, { useState } from 'react';
import { PageHeader } from '../../components/ui';

const faqs = [
  {
    category: '📋 Registration',
    items: [
      {
        q: 'How do I register as a citizen?',
        a: 'Click "Register" on the homepage, fill in your personal details (name, email, password, phone, date of birth, gender, address), and submit the form. Your account will be created with INACTIVE status.'
      },
      {
        q: 'Why is my account INACTIVE after registration?',
        a: 'All new accounts start as INACTIVE for security purposes. You need to upload your identity documents (ID Proof and Residence Proof) and have them verified by an administrator to activate your account.'
      },
      {
        q: 'What information do I need to register?',
        a: 'You need: Full Name, Email Address, Password (min 8 characters), Phone Number (10 digits), Date of Birth, Gender, Address, and Contact Information.'
      }
    ]
  },
  {
    category: '📄 Documents',
    items: [
      {
        q: 'What documents do I need to upload?',
        a: 'You need to upload two types of documents: 1) ID Proof (e.g., Aadhar Card, Passport, Voter ID, Driving License) and 2) Residence Proof (e.g., Utility Bill, Bank Statement, Rent Agreement).'
      },
      {
        q: 'What file formats are accepted?',
        a: 'We accept PDF, JPG, JPEG, and PNG files. Maximum file size is 10MB per document.'
      },
      {
        q: 'How long does document verification take?',
        a: 'Document verification is typically completed within 1-3 business days. A city administrator or service officer will review your uploaded documents.'
      },
      {
        q: 'What happens if my document is rejected?',
        a: 'If a document is rejected, you will see the rejection reason in your Documents page. You can upload a new, corrected document to replace it.'
      },
      {
        q: 'Can I upload documents after my account is active?',
        a: 'Documents can only be uploaded while your account status is INACTIVE. Once your account is activated, document upload is disabled.'
      }
    ]
  },
  {
    category: '🔐 Account Status',
    items: [
      {
        q: 'What are the different account statuses?',
        a: 'There are three statuses: INACTIVE (new account, awaiting document verification), ACTIVE (verified and fully functional), and SUSPENDED (deactivated by an administrator).'
      },
      {
        q: 'How do I activate my account?',
        a: 'Follow these steps: 1) Register your account, 2) Upload ID Proof and Residence Proof, 3) Wait for administrator verification, 4) Once both documents are verified, your account is automatically activated.'
      },
      {
        q: 'Why was my account suspended?',
        a: 'Account suspension is done by a City Administrator. This may happen due to policy violations, fraudulent documents, or other administrative reasons. Contact your local civic office for more details.'
      },
      {
        q: 'Can I reactivate a suspended account?',
        a: 'Suspended accounts can only be reactivated by a City Administrator. Please visit your local civic office with valid identification for assistance.'
      }
    ]
  },
  {
    category: '👤 Profile Management',
    items: [
      {
        q: 'What profile information can I update?',
        a: 'You can update your Address, Contact Information, and Phone Number. Other fields like Name, Email, Date of Birth, and Gender cannot be changed after registration for security reasons.'
      },
      {
        q: 'How do I edit my profile?',
        a: 'Go to your Profile page and click the "Edit Profile" button. Update the desired fields and click "Save Changes".'
      }
    ]
  },
  {
    category: '🆘 Troubleshooting',
    items: [
      {
        q: 'I forgot my password. What should I do?',
        a: 'Currently, password reset needs to be handled by contacting your local civic office. A City Administrator can assist you with password recovery.'
      },
      {
        q: 'I\'m getting "Service Unavailable" errors.',
        a: 'This usually means the backend services are temporarily down or undergoing maintenance. Please wait a few minutes and try again. If the issue persists, contact support.'
      },
      {
        q: 'My document upload is failing.',
        a: 'Make sure your file is in an accepted format (PDF, JPG, JPEG, PNG) and under 10MB. Also ensure your account status is INACTIVE — active accounts cannot upload new documents.'
      }
    ]
  }
];

function HelpPage() {
  const [openItems, setOpenItems] = useState({});

  const toggleItem = (key) => {
    setOpenItems(prev => ({ ...prev, [key]: !prev[key] }));
  };

  return (
    <>
      <PageHeader icon="❓" title="Help Center & FAQ" subtitle="Find answers to frequently asked questions about registration, documents, account status, and more." />

      {faqs.map((section, si) => (
        <div className="card" key={si}>
          <div className="card-title">
            <span className="icon icon-blue">{section.category.split(' ')[0]}</span>
            {section.category.substring(section.category.indexOf(' ') + 1)}
          </div>
          {section.items.map((item, ii) => {
            const key = `${si}-${ii}`;
            return (
              <div className={`faq-item ${openItems[key] ? 'open' : ''}`} key={key}>
                <button className="faq-question" onClick={() => toggleItem(key)}>
                  <span>{item.q}</span>
                  <span className="faq-toggle">+</span>
                </button>
                <div className="faq-answer">
                  {item.a}
                </div>
              </div>
            );
          })}
        </div>
      ))}

      {/* Contact Info */}
      <div className="card">
        <div className="card-title">
          <span className="icon icon-green">📞</span>
          Still need help?
        </div>
        <div className="profile-grid">
          <div className="profile-item">
            <label>📧 Email Support</label>
            <div className="value">support@civicconnect.gov</div>
          </div>
          <div className="profile-item">
            <label>📞 Phone Support</label>
            <div className="value">1800-CIVIC-HELP (1800-248-4243)</div>
          </div>
          <div className="profile-item">
            <label>🕐 Working Hours</label>
            <div className="value">Mon - Fri, 9:00 AM - 5:00 PM</div>
          </div>
          <div className="profile-item">
            <label>📍 Visit Us</label>
            <div className="value">City Municipal Office, Main Road</div>
          </div>
        </div>
      </div>
    </>
  );
}

export default HelpPage;

