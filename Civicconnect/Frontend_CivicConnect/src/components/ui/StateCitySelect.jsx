import React from 'react';
import { INDIA_STATES, getCitiesForState } from '../../data/india-states-cities';

/**
 * StateCitySelect — cascading State → City dropdowns.
 *
 * Two side-by-side <select>s. Choosing a state filters the city options.
 * If the user changes the state, the city is reset (passed up as '').
 *
 * Props:
 *   state       — currently selected state (string).
 *   city        — currently selected city  (string).
 *   onChange    — function({ state, city }). Receives BOTH values together
 *                 so the parent component does only one setState call.
 *   required    — make both selects required.
 *   disabled    — disable both selects.
 *   stateLabel  — optional override for the state field label.
 *   cityLabel   — optional override for the city field label.
 */
export default function StateCitySelect({
  state,
  city,
  onChange,
  required = true,
  disabled = false,
  stateLabel = 'State',
  cityLabel  = 'City',
}) {
  const cities = getCitiesForState(state);

  const handleStateChange = (e) => {
    // When state changes, the previously selected city is no longer valid —
    // reset it so the parent always has a valid (state, city) pair.
    onChange({ state: e.target.value, city: '' });
  };

  const handleCityChange = (e) => {
    onChange({ state, city: e.target.value });
  };

  return (
    <div className="form-row">
      <div className="form-group">
        <label>{stateLabel}{required && ' *'}</label>
        <select
          value={state || ''}
          onChange={handleStateChange}
          required={required}
          disabled={disabled}
        >
          <option value="" disabled>— Select state —</option>
          {INDIA_STATES.map(s => (
            <option key={s} value={s}>{s}</option>
          ))}
        </select>
      </div>

      <div className="form-group">
        <label>{cityLabel}{required && ' *'}</label>
        <select
          value={city || ''}
          onChange={handleCityChange}
          required={required}
          disabled={disabled || !state}
        >
          <option value="" disabled>
            {state ? '— Select city —' : '— Choose state first —'}
          </option>
          {cities.map(c => (
            <option key={c} value={c}>{c}</option>
          ))}
        </select>
      </div>
    </div>
  );
}
