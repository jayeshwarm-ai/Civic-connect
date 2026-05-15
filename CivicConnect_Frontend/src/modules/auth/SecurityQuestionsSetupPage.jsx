import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getSecurityQuestions, setupSecurityAnswers } from '../../services/api';
import { toast } from 'react-toastify';
import { PageHeader, FieldError } from '../../components/ui';

/**
 * Citizen-facing page where a user picks 3 security questions and provides
 * answers used later for password recovery (forgot-password flow).
 *
 * The user may run this page more than once — calling setup again replaces
 * their existing answers entirely (backend deletes the old rows first).
 */
export default function SecurityQuestionsSetupPage() {
  const navigate = useNavigate();
  const [allQuestions, setAllQuestions] = useState([]);
  const [picks, setPicks] = useState([
    { questionId: '', answer: '' },
    { questionId: '', answer: '' },
    { questionId: '', answer: '' },
  ]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [errors, setErrors] = useState({});

  useEffect(() => {
    (async () => {
      try {
        const res = await getSecurityQuestions();
        setAllQuestions(res.data || []);
      } catch {
        toast.error('Failed to load security questions.');
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  // For each dropdown, hide questions already chosen in the OTHER dropdowns
  // so the user can't pick the same question twice.
  const optionsFor = (index) => {
    const otherIds = picks
      .filter((_, i) => i !== index)
      .map(p => p.questionId)
      .filter(Boolean);
    return allQuestions.filter(q => !otherIds.includes(String(q.questionId)));
  };

  const updatePick = (index, field, value) => {
    const next = [...picks];
    next[index] = { ...next[index], [field]: value };
    setPicks(next);
    if (errors[`${field}_${index}`]) {
      setErrors({ ...errors, [`${field}_${index}`]: undefined });
    }
  };

  const validateForm = () => {
    const next = {};
    picks.forEach((p, i) => {
      if (!p.questionId) next[`questionId_${i}`] = 'Choose a question.';
      if (!p.answer || !p.answer.trim()) next[`answer_${i}`] = 'Answer is required.';
      else if (p.answer.trim().length < 2) next[`answer_${i}`] = 'Answer must be at least 2 characters.';
    });
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (!validateForm()) return;

    setSaving(true);
    try {
      // Backend expects an `answers` array of { questionId, answer }
      const payload = picks.map(p => ({
        questionId: Number(p.questionId),
        answer: p.answer.trim(),
      }));
      await setupSecurityAnswers(payload);
      toast.success('Security questions saved. You can use them to recover your password.');
      navigate('/profile');
    } catch (err) {
      setError(err.response?.data?.message || 'Could not save security questions.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="loading"><div className="spinner"></div><br/>Loading questions...</div>;
  }

  return (
    <>
      <PageHeader
        title="Security Questions"
        subtitle="Set up 3 questions used to verify your identity if you ever forget your password."
      />

      <div className="card" style={{ maxWidth: 720 }}>
        <div className="info-tip" style={{ width: '100%', marginBottom: 20 }}>
          <span className="tip-icon"></span>
          Choose questions only you would know the answer to. Answers are <strong>not case-sensitive</strong> and ignore extra spaces.
          Saving here <strong>replaces</strong> any questions you previously set.
        </div>

        {error && <div className="error-msg">{error}</div>}

        <form onSubmit={handleSubmit} noValidate>
          {picks.map((pick, i) => (
            <div key={i} style={{ borderTop: i > 0 ? '1px solid var(--gray-100)' : 'none', paddingTop: i > 0 ? 16 : 0, marginTop: i > 0 ? 16 : 0 }}>
              <div className="form-group">
                <label>Question {i + 1}</label>
                <select
                  value={pick.questionId}
                  onChange={e => updatePick(i, 'questionId', e.target.value)}
                  className={errors[`questionId_${i}`] ? 'input-error' : ''}
                >
                  <option value="">— Choose a question —</option>
                  {optionsFor(i).map(q => (
                    <option key={q.questionId} value={q.questionId}>{q.questionText}</option>
                  ))}
                </select>
                <FieldError message={errors[`questionId_${i}`]} />
              </div>
              <div className="form-group">
                <label>Your Answer</label>
                <input
                  type="text"
                  value={pick.answer}
                  onChange={e => updatePick(i, 'answer', e.target.value)}
                  placeholder="Type your answer"
                  className={errors[`answer_${i}`] ? 'input-error' : ''}
                />
                <FieldError message={errors[`answer_${i}`]} />
              </div>
            </div>
          ))}

          <div className="actions-row">
            <button className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving...' : 'Save Questions'}
            </button>
            <button type="button" className="btn btn-outline" onClick={() => navigate(-1)}>
              Cancel
            </button>
          </div>
        </form>
      </div>
    </>
  );
}
