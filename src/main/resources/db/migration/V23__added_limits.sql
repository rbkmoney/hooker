ALTER TABLE hook.party_key RENAME TO party_data;
ALTER TABLE hook.party_data ADD COLUMN metadata CHARACTER VARYING;