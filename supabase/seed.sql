-- ====================================================================
-- SEED DATA: 8 REALISTIC SAMPLE BUSINESSES IN HIMMATNAGAR, GUJARAT
-- (Clearly marked demo data, easy to delete or replace)
-- ====================================================================

-- 1. Insert Demo Profiles for Owners
INSERT INTO public.profiles (id, phone, role, name)
VALUES 
    ('11111111-1111-1111-1111-111111111111', '+919825011223', 'owner', 'Rajeshbhai Shah'),
    ('22222222-2222-2222-2222-222222222222', '+919898144556', 'owner', 'Pankajbhai Patel'),
    ('33333333-3333-3333-3333-333333333333', '+919427055667', 'owner', 'Meenaben Dave'),
    ('44444444-4444-4444-4444-444444444444', '+919825477889', 'owner', 'Ghanshyambhai Joshi'),
    ('55555555-5555-5555-5555-555555555555', '+919726088990', 'owner', 'Dr. Ketan Modi'),
    ('66666666-6666-6666-6666-666666666666', '+919428533221', 'owner', 'Hiteshbhai Suthar'),
    ('77777777-7777-7777-7777-777777777777', '+919824266778', 'owner', 'Ambalal Soni'),
    ('88888888-8888-8888-8888-888888888888', '+919909922334', 'owner', 'Vikramsinh Vaghela')
ON CONFLICT (phone) DO NOTHING;

-- 2. Insert Businesses
INSERT INTO public.businesses (id, owner_id, name, category, phone, whatsapp, area, address, lat, lng, bio, is_approved)
VALUES
    ('a1111111-0000-0000-0000-000000000001', '11111111-1111-1111-1111-111111111111', 'Mahavir Mobile & Gadgets', 'Mobile & Electronics', '+919825011223', '919825011223', 'Mahavirnagar', 'Shop 4, City Centre, Near Mahavirnagar Circle, Himmatnagar', 23.5991, 72.9642, 'Authorised dealer for Apple, OnePlus, Samsung & Vivo. Genuine accessories and express repair.', true),
    ('a2222222-0000-0000-0000-000000000002', '22222222-2222-2222-2222-222222222222', 'Shreeji Kathiyawadi & Punjabi Dhaba', 'Food', '+919898144556', '919898144556', 'Motipura', 'NH-8 Highway, Near Motipura Circle, Himmatnagar', 23.5934, 72.9712, 'Famous authentic Gujarati Thali, Ringna No Olo, Bajra Roti with White Butter & Sizzling Punjabi dishes.', true),
    ('a3333333-0000-0000-0000-000000000003', '33333333-3333-3333-3333-333333333333', 'Royal Heritage Fashion & Kurtis', 'Fashion', '+919427055667', '919427055667', 'Station Road', 'Station Road, Opp. S.T. Bus Depot, Himmatnagar', 23.5960, 72.9610, 'Designer Chaniya Cholis, Festive Sarees, Wedding Sherwanis & Trendy Daily Wear Kurtis.', true),
    ('a4444444-0000-0000-0000-000000000004', '44444444-4444-4444-4444-444444444444', 'Radhe Krishna Sweets & Farsan', 'Food', '+919825477889', '919825477889', 'Tower Chowk', 'Clock Tower Circle, Main Bazaar, Himmatnagar', 23.5982, 72.9675, 'Famous Himmatnagar Fafda Jalebi, Peda, Kaju Katli & pure desi ghee sweets since 1984.', true),
    ('a5555555-0000-0000-0000-000000000005', '55555555-5555-5555-5555-555555555555', 'Apex 24/7 Pharmacy & Wellness', 'Medical', '+919726088990', '919726088990', 'Tower Chowk', 'Near Civil Hospital Road, Tower Chowk, Himmatnagar', 23.5979, 72.9690, 'Open 24 Hours. All prescription medicines, surgical items, baby care, orthopedic aids & home delivery.', true),
    ('a6666666-0000-0000-0000-000000000006', '66666666-6666-6666-6666-666666666666', 'Polo Green Mart & Dry Fruits', 'Grocery', '+919428533221', '919428533221', 'Polo Ground', 'Polo Ground Shopping Arcade, Himmatnagar', 23.6015, 72.9630, 'Direct farm produce, organic spices, cold-pressed oils, premium California almonds & dry fruits.', true),
    ('a7777777-0000-0000-0000-000000000007', '77777777-7777-7777-7777-777777777777', 'Shree Ambica Jewellers', 'Jewelry', '+919824266778', '919824266778', 'Station Road', 'Sona Bazaar, Station Road, Himmatnagar', 23.5955, 72.9618, '100% BIS Hallmarked 916 Gold jewellery, certified Solitaire rings, bridal necklace sets & custom craft.', true),
    ('a8888888-0000-0000-0000-000000000008', '88888888-8888-8888-8888-888888888888', 'Krishna Auto Spa & Car Care', 'Auto & Vehicles', '+919909922334', '9909922334', 'Bypass Road', 'Bypass Ring Road, Near HP Petrol Pump, Himmatnagar', 23.5910, 72.9750, 'High-pressure snow foam wash, interior dry cleaning, 9H ceramic coating & wheel alignment.', true)
ON CONFLICT (id) DO NOTHING;

-- 3. Insert Offers (Deals Finder)
INSERT INTO public.offers (business_id, product_name, original_price, discounted_price, discount_percent, coupon_code, valid_until)
VALUES
    ('a1111111-0000-0000-0000-000000000001', 'iPhone 18 Pro (256GB)', 134900, 119900, 12, 'HIMMATIPHONE', 'Oct 15, 2026'),
    ('a1111111-0000-0000-0000-000000000001', 'Smart Fitness Band Watch 9 AMOLED', 3999, 1499, 62, 'SMART62', 'Oct 08, 2026'),
    ('a2222222-0000-0000-0000-000000000002', 'Unlimited Gujarati Kathiyawadi Thali', 350, 199, 43, 'DESITHALI', 'Oct 10, 2026'),
    ('a3333333-0000-0000-0000-000000000003', 'Festive Cotton Embroidered Kurti Set', 1499, 699, 53, 'FESTIVE50', 'Oct 20, 2026'),
    ('a5555555-0000-0000-0000-000000000005', 'Full Body Comprehensive Health Test Package', 1999, 799, 60, 'CARE60', 'Oct 30, 2026'),
    ('a6666666-0000-0000-0000-000000000006', 'Premium California Almonds + Walnuts Combo (1kg)', 1500, 950, 37, 'POLONUTS', 'Oct 12, 2026'),
    ('a4444444-0000-0000-0000-000000000004', 'Pure Desi Ghee Kaju Katli (500g Gift Box)', 550, 380, 31, 'SWEET30', 'Oct 05, 2026'),
    ('a8888888-0000-0000-0000-000000000008', 'Complete Exterior Ceramic Foam Detailing', 9000, 4999, 44, 'AUTOSPA', 'Oct 18, 2026');

-- 4. Insert Reels with 10-Day Expiry Timestamps
INSERT INTO public.reels (business_id, video_url, title, caption, views, likes, created_at, expires_at)
VALUES
    ('a1111111-0000-0000-0000-000000000001', 'https://example.com/videos/iphone_himmatnagar.mp4', 'New iPhone 18 Pro Unboxing & Festive Launch Event!', 'Now in stock at Mahavir Mobile Himmatnagar! Flat ₹15,000 exchange bonus today.', 1840, 340, NOW() - INTERVAL '1 day', NOW() + INTERVAL '9 days'),
    ('a2222222-0000-0000-0000-000000000002', 'https://example.com/videos/olo_rotla_shreeji.mp4', 'Fresh Ringna No Olo & Hot Bajra Rotla Live Preparation', 'Charcoal-roasted smoke flavour with organic white butter! Unlimited Thali ₹199 only.', 4320, 890, NOW() - INTERVAL '3 days', NOW() + INTERVAL '7 days'),
    ('a3333333-0000-0000-0000-000000000003', 'https://example.com/videos/bridal_collection.mp4', 'Bridal Chaniya Choli & Navratri Designer Collection', 'Pure silk and authentic mirror work straight from our workshop on Station Road.', 2950, 620, NOW() - INTERVAL '2 days', NOW() + INTERVAL '8 days'),
    ('a4444444-0000-0000-0000-000000000004', 'https://example.com/videos/fafda_jalebi.mp4', 'Morning Hot Fafda Jalebi & Kadhi Making at Tower Chowk', 'Crispy fafda and piping hot saffron jalebi. Himmatnagar landmark breakfast since 1984!', 6100, 1250, NOW() - INTERVAL '4 days', NOW() + INTERVAL '6 days');

-- 5. Insert Worker Directory Listings
INSERT INTO public.workers (name, service_type, phone, area, experience, rating)
VALUES
    ('Ramesh Solanki', 'Plumber', '+919825190812', 'Motipura', '12 Years Exp', 4.9),
    ('Jignesh Panchal', 'Electrician', '+919723041526', 'Mahavirnagar', '8 Years Exp', 4.8),
    ('Bharat Prajapati', 'AC Repair & Technician', '+919904211984', 'Station Road', '6 Years Exp', 4.7),
    ('Sunita Patel', 'Mathematics & Science Tutor', '+919426877213', 'Polo Ground', '7 Years Exp', 4.9),
    ('Dinesh Mistri', 'Carpenter & Wood Interior', '+919898033412', 'Tower Chowk', '15 Years Exp', 4.8);
