INSERT INTO apartments(flat_number,block_name,floor_number,monthly_charge)
VALUES ('A-101','A',1,2500),('A-102','A',1,2500),('B-201','B',2,3000)
ON CONFLICT(flat_number) DO NOTHING;
INSERT INTO residents(full_name,phone,email,apartment_id)
SELECT 'Arun Kumar','9876543210','arun@example.com',apartment_id FROM apartments WHERE flat_number='A-101'
ON CONFLICT(email) DO NOTHING;
INSERT INTO residents(full_name,phone,email,apartment_id)
SELECT 'Meena Devi','9876501234','meena@example.com',apartment_id FROM apartments WHERE flat_number='A-102'
ON CONFLICT(email) DO NOTHING;
INSERT INTO maintenance_bills(resident_id,bill_month,amount,payment_status,paid_amount,paid_on)
SELECT resident_id,DATE '2026-11-01',2500,'PAID',2500,CURRENT_DATE FROM residents WHERE email='arun@example.com'
ON CONFLICT(resident_id,bill_month) DO NOTHING;
INSERT INTO maintenance_bills(resident_id,bill_month,amount)
SELECT resident_id,DATE '2026-11-01',2500 FROM residents WHERE email='meena@example.com'
ON CONFLICT(resident_id,bill_month) DO NOTHING;
INSERT INTO complaints(resident_id,subject,description,status)
SELECT resident_id,'Water leakage','Leakage reported near kitchen sink','OPEN'
FROM residents WHERE email='meena@example.com';
