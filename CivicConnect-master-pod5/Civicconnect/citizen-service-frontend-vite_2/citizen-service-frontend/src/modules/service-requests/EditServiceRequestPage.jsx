import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getServiceRequestById } from '../../services/api';
import api from '../../services/api';
import { toast } from 'react-toastify';
import { PageHeader, StateCitySelect, FieldError } from '../../components/ui';
import { validate, required, minLen, maxLen } from '../../utils/validators';

const RULES = {
  state:       [required('State')],
  city:        [required('City')],
  address:     [required('Address'), minLen(15, 'Address'), maxLen(255, 'Address')],
  description: [required('Description'), minLen(10, 'Description'), maxLen(1000, 'Description')],
};

export default function EditServiceRequestPage() {
  const { requestId } = useParams();
  const navigate = useNavigate();
  const [form, setForm] = useState({ type: '', description: '', state: '', city: '', address: '' });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [serverError, setServerError] = useState('');

  useEffect(() => {
    (async () => {
      try {
        const res = await getServiceRequestById(requestId);
        if (res.data.status !== 'SUBMITTED') {
          toast.error('Only SUBMITTED requests can be edited.');
          navigate('/service-requests');
          return;
        }
        setForm({
          type: res.data.type,
          description: res.data.description,
          state:   res.data.state   || '',
          city:    res.data.city    || '',
          address: res.data.address || '',
        });
      } catch {
        toast.error('Failed to load request.');
      } finally {
        setLoading(false);
      }
    })();
  }, [requestId, navigate]);

  const setField = (field, value) => {
    const next = { ...form, [field]: value };
    setForm(next);
    if (errors[field]) {
      const fe = validate(next, { [field]: RULES[field] || [] });
      setErrors({ ...errors, [field]: fe[field] });
    }
  };

  const handleStateCity = ({ state, city }) => {
    const next = { ...form, state, city };
    setForm(next);
    if (errors.state || errors.city) {
      const fe = validate(next, { state: RULES.state, city: RULES.city });
      setErrors({ ...errors, state: fe.state, city: fe.city });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const formErrors = validate(form, RULES);
    setErrors(formErrors);
    if (Object.keys(formErrors).length > 0) {
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }
    setServerError('');
    setSaving(true);
    try {
      await api.put(`/api/v1/service-requests/${requestId}`, form);
      toast.success('✅ Request updated!');
      navigate('/service-requests');
    } catch (err) {
      setServerError(err.response?.data?.message || 'Update failed.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div className="loading"><div className="spinner"></div><br/>Loading...</div>;

  return (
    <>
      <PageHeader
        icon="✏️"
        title={<>Edit Service Request #{requestId}</>}
        subtitle="Update your submitted service request details."
      />
      <div className="card">
        <div className="card-title"><span className="icon icon-blue">✏️</span> Edit Request</div>

        {serverError && <div className="error-msg">⚠️ {serverError}</div>}

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label>Request Type *</label>
            <select value={form.type} onChange={e => setField('type', e.target.value)}>
              <option value="ROAD">🛣️ Road</option>
              <option value="WATER">💧 Water</option>
              <option value="ELECTRICITY">⚡ Electricity</option>
            </select>
          </div>

          <StateCitySelect state={form.state} city={form.city} onChange={handleStateCity} />
          <FieldError message={errors.state} />
          <FieldError message={errors.city} />

          <div className="form-group">
            <label>Address *</label>
            <textarea
              value={form.address}
              onChange={e => setField('address', e.target.value)}
              rows={2}
              placeholder="Street, area, landmark"
              className={errors.address ? 'input-error' : ''}
            />
            <FieldError message={errors.address} />
          </div>

          <div className="form-group">
            <label>Description *</label>
            <textarea
              value={form.description}
              onChange={e => setField('description', e.target.value)}
              rows={4}
              className={errors.description ? 'input-error' : ''}
            />
            <FieldError message={errors.description} />
          </div>

          <div className="actions-row">
            <button className="btn btn-primary" disabled={saving}>
              {saving ? '⏳ Saving...' : '💾 Save Changes'}
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
