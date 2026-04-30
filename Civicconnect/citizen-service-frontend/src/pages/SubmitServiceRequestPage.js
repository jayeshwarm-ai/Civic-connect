import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { submitServiceRequest } from '../services/api';
import { toast } from 'react-toastify';

export default function SubmitServiceRequestPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ type: 'ROAD', description: '', location: '' });
  const [loading, setLoading] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (submitted || loading) return;
    setError(''); setLoading(true);
    try {
      await submitServiceRequest(form);
      setSubmitted(true);
      toast.success('🎉 Service request submitted!');
      navigate('/service-requests');
    } catch (err) {
      setError(err.response?.data?.message || 'Submission failed. Make sure your account is ACTIVE.');
    } finally { setLoading(false); }
  };

  return (
    <>
      <div className="welcome-banner" style={{background:'linear-gradient(135deg, var(--primary-dark) 0%, var(--primary) 100%)'}}>
        <h2>➕ Submit Service Request</h2>
        <p>Report an issue with road, water, or electricity services in your area.</p>
      </div>
      <div className="card">
        <div className="card-title"><span className="icon icon-blue">📝</span> New Request</div>
        {error && <div className="error-msg">⚠️ {error}</div>}
        <div className="info-tip" style={{width:'100%',marginBottom:20}}>
          <span className="tip-icon">💡</span> Your account must be <strong>ACTIVE</strong> (documents verified) to submit requests.
        </div>
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group">
              <label>Request Type</label>
              <select value={form.type} onChange={e=>setForm({...form,type:e.target.value})}>
                <option value="ROAD">🛣️ Road</option>
                <option value="WATER">💧 Water</option>
                <option value="ELECTRICITY">⚡ Electricity</option>
              </select>
            </div>
            <div className="form-group">
              <label>Location</label>
              <input value={form.location} onChange={e=>setForm({...form,location:e.target.value})} placeholder="e.g., 123 Main St, Ward 5" required />
            </div>
          </div>
          <div className="form-group">
            <label>Description</label>
            <textarea value={form.description} onChange={e=>setForm({...form,description:e.target.value})} rows={4} placeholder="Describe the issue in detail..." required />
          </div>
          <div className="actions-row">
            <button className="btn btn-primary" disabled={loading || submitted}>{loading ? '⏳ Submitting...' : submitted ? '✅ Submitted' : '📨 Submit Request'}</button>
            <button type="button" className="btn btn-outline" onClick={()=>navigate('/service-requests')}>Cancel</button>
          </div>
        </form>
      </div>
    </>
  );
}

