import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { submitServiceRequest } from '../../services/api';
import { toast } from 'react-toastify';
import { PageHeader, StateCitySelect, FieldError } from '../../components/ui';
import { validate, required, minLen, maxLen } from '../../utils/validators';

const RULES = {
  state:       [required('State')],
  city:        [required('City')],
  address:     [required('Address'), minLen(15, 'Address'), maxLen(255, 'Address')],
  description: [required('Description'), minLen(10, 'Description'), maxLen(1000, 'Description')],
};

export default function SubmitServiceRequestPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    type: 'ROAD',
    description: '',
    state: '',
    city: '',
    address: '',
  });
  const [errors,    setErrors]    = useState({});
  const [loading,   setLoading]   = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [serverError, setServerError] = useState('');

  // Re-validate a single field as the user edits — only if it already has an
  // error visible. Stops the message from sticking around after the user fixes it.
  const setField = (field, value) => {
    const next = { ...form, [field]: value };
    setForm(next);
    if (errors[field]) {
      const fieldErrors = validate(next, { [field]: RULES[field] || [] });
      setErrors({ ...errors, [field]: fieldErrors[field] });
    }
  };

  const handleStateCity = ({ state, city }) => {
    const next = { ...form, state, city };
    setForm(next);
    if (errors.state || errors.city) {
      const fieldErrors = validate(next, { state: RULES.state, city: RULES.city });
      setErrors({ ...errors, state: fieldErrors.state, city: fieldErrors.city });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (submitted || loading) return;

    // Validate the whole form before hitting the API.
    const formErrors = validate(form, RULES);
    setErrors(formErrors);
    if (Object.keys(formErrors).length > 0) {
      // Scroll to the first error so the user sees it on small screens.
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }

    setServerError('');
    setLoading(true);
    try {
      await submitServiceRequest(form);
      setSubmitted(true);
      toast.success('🎉 Service request submitted!');
      navigate('/service-requests');
    } catch (err) {
      setServerError(err.response?.data?.message || 'Submission failed. Make sure your account is ACTIVE.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <PageHeader
        icon="➕"
        title="Submit Service Request"
        subtitle="Report an issue with road, water, or electricity services in your area."
      />
      <div className="card">
        <div className="card-title"><span className="icon icon-blue">📝</span> New Request</div>

        {serverError && <div className="error-msg">⚠️ {serverError}</div>}

        <div className="info-tip" style={{ width: '100%', marginBottom: 20 }}>
          <span className="tip-icon">💡</span>
          Your account must be <strong>ACTIVE</strong> (documents verified) to submit requests.
        </div>

        <form onSubmit={handleSubmit} noValidate>
          {/* Request type — always valid via the dropdown */}
          <div className="form-group">
            <label>Request Type *</label>
            <select value={form.type} onChange={e => setField('type', e.target.value)}>
              <option value="ROAD">🛣️ Road</option>
              <option value="WATER">💧 Water</option>
              <option value="ELECTRICITY">⚡ Electricity</option>
            </select>
          </div>

          {/* State + City cascading dropdowns. Errors render after the row. */}
          <StateCitySelect state={form.state} city={form.city} onChange={handleStateCity} />
          <FieldError message={errors.state} />
          <FieldError message={errors.city} />

          {/* Address */}
          <div className="form-group">
            <label>Address *</label>
            <textarea
              value={form.address}
              onChange={e => setField('address', e.target.value)}
              rows={2}
              placeholder="Street, area, landmark — e.g., 123 Main Street, near Ward 5 office"
              className={errors.address ? 'input-error' : ''}
            />
            <FieldError message={errors.address} />
          </div>

          {/* Description */}
          <div className="form-group">
            <label>Description *</label>
            <textarea
              value={form.description}
              onChange={e => setField('description', e.target.value)}
              rows={4}
              placeholder="Describe the issue in detail..."
              className={errors.description ? 'input-error' : ''}
            />
            <FieldError message={errors.description} />
          </div>

          <div className="actions-row">
            <button className="btn btn-primary" disabled={loading || submitted}>
              {loading ? '⏳ Submitting...' : submitted ? '✅ Submitted' : '📨 Submit Request'}
            </button>
            <button type="button" className="btn btn-outline" onClick={() => navigate('/service-requests')}>
              Cancel
            </button>
          </div>
        </form>
      </div>
    </>
  );
}
