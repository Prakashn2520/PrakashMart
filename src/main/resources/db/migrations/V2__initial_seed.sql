-- ==========================================================
-- PrakashMart — Migration V2: Initial Seed Data
-- Created: 2026-09-20
-- ==========================================================
-- Passwords are BCrypt hashes (cost=12):
-- admin123  -> $2a$12$qKzk6H7WGdb2WVpX3bKQmOG9DhO7DnpWKM/Aapv1MW1ylFiwrQkre
-- seller123 -> $2a$12$M6a7NKDtDAoxaoohwBHYOuQTz0NU7PTZYrMPx17Zg2iUVXhM6b0SW
-- buyer123  -> $2a$12$2PLRursg3L62LU/8ALbIauXz7bAZM6jlXfqiXq33VO1C1ttFavB6u
-- ==========================================================

INSERT INTO users (name, email, password_hash, role) VALUES
  ('Admin Prakash',       'admin@prakashmart.com',   '$2a$12$qKzk6H7WGdb2WVpX3bKQmOG9DhO7DnpWKM/Aapv1MW1ylFiwrQkre', 'ADMIN'),
  ('Prakash Electronics', 'seller1@prakashmart.com', '$2a$12$M6a7NKDtDAoxaoohwBHYOuQTz0NU7PTZYrMPx17Zg2iUVXhM6b0SW', 'SELLER'),
  ('BookWorld Store',     'seller2@prakashmart.com', '$2a$12$M6a7NKDtDAoxaoohwBHYOuQTz0NU7PTZYrMPx17Zg2iUVXhM6b0SW', 'SELLER'),
  ('Alice Buyer',         'buyer1@prakashmart.com',  '$2a$12$2PLRursg3L62LU/8ALbIauXz7bAZM6jlXfqiXq33VO1C1ttFavB6u', 'BUYER'),
  ('Bob Buyer',           'buyer2@prakashmart.com',  '$2a$12$2PLRursg3L62LU/8ALbIauXz7bAZM6jlXfqiXq33VO1C1ttFavB6u', 'BUYER');

-- Electronics
INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url) VALUES
  (2, 'Mechanical Keyboard',
   'Compact TKL mechanical keyboard with Cherry MX Brown switches and per-key RGB backlighting.',
   89.99, 25, 'Electronics',
   'https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=500'),
  (2, 'Wireless Noise-Cancelling Headphones',
   'Over-ear Bluetooth headphones. 40 mm drivers, 30 h battery, foldable design.',
   149.50, 15, 'Electronics',
   'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500'),
  (2, 'USB-C Hub (7-in-1)',
   '4K HDMI, 3x USB-A 3.0, SD card reader, 100 W PD charging.',
   45.00, 50, 'Electronics',
   'https://images.unsplash.com/photo-1593642632559-0c6d3fc62b89?w=500'),
  (2, 'Portable Bluetooth Speaker',
   'IPX7 waterproof, 360 degree surround sound, 20 h playtime.',
   59.99, 30, 'Electronics',
   'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=500'),
  (2, 'Laptop Stand (Aluminium)',
   'Adjustable ergonomic stand compatible with 10-17 inch laptops.',
   34.99, 60, 'Electronics',
   'https://images.unsplash.com/photo-1593642634491-a5fa8eb5b1ac?w=500');

-- Books
INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url) VALUES
  (3, 'Clean Code',
   'By Robert C. Martin - A handbook of agile software craftsmanship.',
   39.95, 50, 'Books',
   'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500'),
  (3, 'Effective Java (3rd Edition)',
   'By Joshua Bloch - The definitive guide to best-practice Java programming.',
   44.99, 35, 'Books',
   'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?w=500'),
  (3, 'Designing Data-Intensive Applications',
   'By Martin Kleppmann - Deep dive into distributed systems and databases.',
   49.99, 20, 'Books',
   'https://images.unsplash.com/photo-1510172951991-856a654063f9?w=500');

-- Home & Kitchen
INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url) VALUES
  (3, 'Ceramic Coffee Mug (400 ml)',
   'Handcrafted matte-finish ceramic mug. Dishwasher safe.',
   18.00, 80, 'Home',
   'https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=500'),
  (3, 'Bamboo Desk Organiser',
   'Eco-friendly 5-compartment desk organiser made from sustainable bamboo.',
   27.50, 45, 'Home',
   'https://images.unsplash.com/photo-1593642634315-48f5414c3ad9?w=500');

-- Clothing
INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url) VALUES
  (2, 'Cotton Graphic Tee - Developer Edition',
   '100% combed cotton. Available in S / M / L / XL. Pre-shrunk.',
   22.00, 100, 'Clothing',
   'https://images.unsplash.com/photo-1583743814966-8936f5b7be1a?w=500');

-- Sample Order 1
INSERT INTO orders (buyer_id, status, total_amount) VALUES
  (4, 'DELIVERED', 129.94);

INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
  (1, 1, 1, 89.99),
  (1, 6, 1, 39.95);

-- Sample Order 2
INSERT INTO orders (buyer_id, status, total_amount) VALUES
  (5, 'SHIPPED', 194.49);

INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
  (2, 2, 1, 149.50),
  (2, 7, 1, 44.99);

-- Sample Order 3
INSERT INTO orders (buyer_id, status, total_amount) VALUES
  (4, 'PENDING', 63.00);

INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
  (3, 3, 1, 45.00),
  (3, 9, 1, 18.00);

-- Cart items
INSERT INTO cart_items (user_id, product_id, quantity) VALUES
  (5, 5,  1),
  (5, 10, 2);

-- Reviews
INSERT INTO reviews (product_id, user_id, rating, comment) VALUES
  (1, 4, 5, 'Excellent keyboard! The typing feel is superb and build quality is top-notch.'),
  (6, 4, 5, 'Best programming book I have ever read. A must-have for every developer.'),
  (2, 5, 4, 'Great sound quality and very comfortable. Battery life is impressive.'),
  (7, 5, 5, 'Bloch explains Java best-practices with clear real-world examples.');
