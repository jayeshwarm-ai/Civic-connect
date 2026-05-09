/**
 * Field-level validators.
 *
 * Usage in a component:
 *   const errors = validate(form, {
 *     name:    [required('Name'), minLen(3, 'Name')],
 *     email:   [required('Email'), email()],
 *     phone:   [required('Phone'), exactDigits(10, 'Phone')],
 *     address: [required('Address'), minLen(15, 'Address')],
 *   });
 *   if (Object.keys(errors).length) { setErrors(errors); return; }
 *
 * Each rule receives the value and returns either:
 *   - `undefined` (valid)
 *   - a string error message (invalid)
 *
 * `validate(form, rules)` walks the rules and stops at the first failing rule
 * per field, so the user sees one clear message per field at a time.
 */

// ── Reusable rules ───────────────────────────────────────────────────────

export const required = (label = 'This field') => (v) => {
  const empty = v === null || v === undefined || (typeof v === 'string' && v.trim() === '');
  return empty ? `${label} is required.` : undefined;
};

export const minLen = (n, label = 'This field') => (v) => {
  if (v == null) return undefined;
  return String(v).trim().length < n
    ? `${label} should contain at least ${n} characters.`
    : undefined;
};

export const maxLen = (n, label = 'This field') => (v) => {
  if (v == null) return undefined;
  return String(v).length > n
    ? `${label} cannot exceed ${n} characters.`
    : undefined;
};

export const email = () => (v) => {
  if (!v) return undefined;                                 // empty handled by `required`
  // Standard, pragmatic email pattern — same one most form libraries use.
  const re = /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i;
  return re.test(String(v).trim()) ? undefined : 'Please enter a valid email address.';
};

export const exactDigits = (n, label = 'This field') => (v) => {
  if (!v) return undefined;
  const s = String(v).trim();
  if (!/^\d+$/.test(s)) return `${label} must contain only digits.`;
  if (s.length !== n)   return `${label} must be exactly ${n} digits.`;
  return undefined;
};

export const passwordStrength = () => (v) => {
  if (!v) return undefined;
  if (v.length < 8) return 'Password must be at least 8 characters long.';
  if (!/[A-Z]/.test(v)) return 'Password must include at least one uppercase letter.';
  if (!/[a-z]/.test(v)) return 'Password must include at least one lowercase letter.';
  if (!/[0-9]/.test(v)) return 'Password must include at least one digit.';
  return undefined;
};

/** Two values must match (e.g. password + confirmPassword). */
export const matches = (otherValue, label = 'Values') => (v) => {
  if (!v) return undefined;
  return v === otherValue ? undefined : `${label} do not match.`;
};

/** Numeric range (inclusive). */
export const range = (min, max, label = 'Value') => (v) => {
  if (v === '' || v === null || v === undefined) return undefined;
  const n = Number(v);
  if (Number.isNaN(n)) return `${label} must be a number.`;
  if (n < min || n > max) return `${label} must be between ${min} and ${max}.`;
  return undefined;
};

/** Date-of-birth must be in the past and the user must be at least `minAge` years old. */
export const dobAtLeast = (minAge = 0) => (v) => {
  if (!v) return undefined;
  const d = new Date(v);
  if (Number.isNaN(d.getTime())) return 'Please enter a valid date.';
  const today = new Date();
  if (d > today) return 'Date of birth cannot be in the future.';
  if (minAge > 0) {
    const age = (today - d) / (365.25 * 24 * 60 * 60 * 1000);
    if (age < minAge) return `You must be at least ${minAge} years old.`;
  }
  return undefined;
};

// ── Driver ───────────────────────────────────────────────────────────────

/**
 * Run the rule map against the given form. Returns an errors object — empty
 * when everything passes. Each field gets at most one message (the first failure).
 */
export function validate(form, rules) {
  const errors = {};
  for (const field of Object.keys(rules)) {
    const value = form[field];
    for (const rule of rules[field]) {
      const msg = rule(value);
      if (msg) { errors[field] = msg; break; }
    }
  }
  return errors;
}
