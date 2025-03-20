-- Add recurrence fields to tasks table
ALTER TABLE tasks ADD COLUMN is_recurring BOOLEAN DEFAULT FALSE;
ALTER TABLE tasks ADD COLUMN recurrence_type VARCHAR(10);
ALTER TABLE tasks ADD COLUMN recurrence_interval INTEGER;
ALTER TABLE tasks ADD COLUMN recurrence_end_date TIMESTAMP;
ALTER TABLE tasks ADD COLUMN parent_task_id BIGINT;

-- Add foreign key constraint for parent_task_id
ALTER TABLE tasks ADD CONSTRAINT fk_parent_task 
    FOREIGN KEY (parent_task_id) REFERENCES tasks(id) ON DELETE SET NULL;