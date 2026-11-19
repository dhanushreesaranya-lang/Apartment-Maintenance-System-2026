CREATE TABLE IF NOT EXISTS apartments (
 apartment_id SERIAL PRIMARY KEY,
 flat_number VARCHAR(20) NOT NULL UNIQUE,
 block_name VARCHAR(50),
 floor_number INT,
 monthly_charge NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (monthly_charge >= 0)
);
CREATE TABLE IF NOT EXISTS residents (
 resident_id SERIAL PRIMARY KEY,
 full_name VARCHAR(100) NOT NULL,
 phone VARCHAR(20),
 email VARCHAR(120) UNIQUE,
 apartment_id INT REFERENCES apartments(apartment_id) ON DELETE SET NULL,
 move_in_date DATE DEFAULT CURRENT_DATE
);
CREATE TABLE IF NOT EXISTS maintenance_bills (
 bill_id SERIAL PRIMARY KEY,
 resident_id INT NOT NULL REFERENCES residents(resident_id) ON DELETE CASCADE,
 bill_month DATE NOT NULL,
 amount NUMERIC(10,2) NOT NULL CHECK (amount >= 0),
 payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
   CHECK (payment_status IN ('PENDING','PAID','PARTIAL')),
 paid_amount NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (paid_amount >= 0),
 paid_on DATE,
 UNIQUE (resident_id, bill_month)
);
CREATE TABLE IF NOT EXISTS complaints (
 complaint_id SERIAL PRIMARY KEY,
 resident_id INT NOT NULL REFERENCES residents(resident_id) ON DELETE CASCADE,
 subject VARCHAR(150) NOT NULL,
 description TEXT,
 complaint_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 status VARCHAR(20) NOT NULL DEFAULT 'OPEN'
   CHECK (status IN ('OPEN','IN_PROGRESS','RESOLVED'))
);
CREATE INDEX IF NOT EXISTS idx_bills_status ON maintenance_bills(payment_status);
CREATE INDEX IF NOT EXISTS idx_complaints_status ON complaints(status);
CREATE OR REPLACE VIEW payment_report AS
 SELECT b.bill_id,r.full_name,a.flat_number,b.bill_month,b.amount,b.paid_amount,b.payment_status
 FROM maintenance_bills b JOIN residents r ON r.resident_id=b.resident_id
 LEFT JOIN apartments a ON a.apartment_id=r.apartment_id;
CREATE OR REPLACE VIEW complaint_report AS
 SELECT c.complaint_id,r.full_name,a.flat_number,c.subject,c.complaint_date,c.status
 FROM complaints c JOIN residents r ON r.resident_id=c.resident_id
 LEFT JOIN apartments a ON a.apartment_id=r.apartment_id;
