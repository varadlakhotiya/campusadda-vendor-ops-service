CREATE DATABASE campusadda_vendor_ops;

INSERT INTO vendors
(id, vendor_code, name, description, contact_name, contact_phone, contact_email, location_label, campus_area, status, source_system, external_vendor_id)
VALUES
(101, 'CHATKARA', 'Chatkara', 'North Indian meals, biryani, rice bowls, Indo-Chinese dishes, and affordable combo options.', 'Rahul Singh', '9876500001', 'rahul.singh@chatkara.com', 'Below B1', 'GHS', 'ACTIVE', 'VENDOR_OPS', NULL),

(102, 'PIZZA_BAKERS', 'Pizza Bakers', 'Fresh pizzas, continental dishes, desserts, and flavorful chicken sides.', 'Neha kadam', '9876500002', 'neha.kadam@pizzabakers.com', 'Below B1', 'GHS', 'ACTIVE', 'VENDOR_OPS', NULL),

(103, 'KITCHEN_CURRY', 'The Kitchen And Curry', 'North Indian, Chinese, healthy meals, beverages, and limited non-veg options.', 'Rinkesh Verma', '9876500003', 'rinkesh.verma@kitchencurry.com', 'Above G1', 'GHS', 'ACTIVE', 'VENDOR_OPS', NULL),

(104, 'COOK_HOUSE', 'Cook House', 'Homestyle North Indian food, biryani, continental dishes, and beverages.', 'Pooja Mehta', '9876500004', 'pooja.mehta@cookhouse.com', 'Above G1', 'GHS', 'ACTIVE', 'VENDOR_OPS', NULL),

(105, 'TEA_TRADITION', 'Tea Tradition', 'Snacks, chaat, bakery items, desserts, beverages, and quick bites.', 'Suresh Patel', '9876500005', 'suresh.patel@teatradition.com', 'Below B4', 'GHS', 'ACTIVE', 'VENDOR_OPS', NULL),

(106, 'ITALIAN_OVEN', 'The Italian Oven', 'Pizzas, continental food, fast food, and light snacks.', 'Ritu Kapoor', '9876500006', 'ritu.kapoor@italianoven.com', 'Above G1', 'GHS', 'ACTIVE', 'VENDOR_OPS', NULL),

(107, 'CRAZY_CHEF', 'The Crazy Chef', 'Pizzas, Chinese, fast food, snacks, and refreshing beverages.', 'Mohit Desai', '9876500007', 'mohit.desai@crazychef.com', 'Above B1', 'GHS', 'ACTIVE', 'VENDOR_OPS', NULL),

(108, 'STARDOM', 'Stardom', 'Kathi rolls, fast food, pizzas, and Chinese dishes.', 'Ananya Shah', '9876500008', 'ananya.shah@stardom.com', 'Below G2', 'GHS', 'ACTIVE', 'VENDOR_OPS', NULL),

(109, 'DEV_SWEETS_SNACKS', 'Dev Sweets and Snacks', 'Pure veg snacks, sweets, chaat, Chinese, and fast food options.', 'Karan Joshi', '9876500009', 'karan.joshi@devsweets.com', 'Below B4', 'GHS', 'ACTIVE', 'VENDOR_OPS', NULL),

(110, 'CHILLING_POINT', 'Chilling Point', 'Ice creams, thick shakes, cold beverages, and Maggi.', 'Priya Nair', '9876500010', 'priya.nair@chillingpoint.com', 'Below B4', 'GHS', 'ACTIVE', 'VENDOR_OPS', NULL),

(111, 'TEA_POST', 'Tea Post Pvt. Ltd.', 'Tea, beverages, and light snacks for quick breaks.', 'Farhan Ali', '9876500011', 'farhan.ali@teapost.com', 'Below G4', 'GHS', 'ACTIVE', 'VENDOR_OPS', NULL);





-- =========================================================
-- CLEANED MENU CATEGORIES
-- Based strictly on vendor descriptions
-- =========================================================

-- If old menu_categories for these vendors are not yet used in menu_items,
-- you can replace them like this:
DELETE FROM menu_categories
WHERE vendor_id IN (101,102,103,104,105,106,107,108,109,110,111);

-- ---------------------------------------------------------
-- 101 - Chatkara
-- Description: North Indian meals, biryani, rice bowls,
-- Indo-Chinese dishes, affordable combo options
-- ---------------------------------------------------------
INSERT INTO menu_categories
(id, vendor_id, category_name, display_order, is_active, source_system, external_category_id)
VALUES
(1001, 101, 'North Indian', 1, TRUE, 'VENDOR_OPS', NULL),
(1002, 101, 'Indo-Chinese', 2, TRUE, 'VENDOR_OPS', NULL),
(1003, 101, 'Biryani & Rice', 3, TRUE, 'VENDOR_OPS', NULL),
(1004, 101, 'Combos & Meals', 4, TRUE, 'VENDOR_OPS', NULL);

-- ---------------------------------------------------------
-- 102 - Pizza Bakers
-- Description: Fresh pizzas, continental dishes, desserts,
-- and flavorful chicken sides
-- ---------------------------------------------------------
INSERT INTO menu_categories
(id, vendor_id, category_name, display_order, is_active, source_system, external_category_id)
VALUES
(1006, 102, 'Pizzas', 1, TRUE, 'VENDOR_OPS', NULL),
(1007, 102, 'Continental', 2, TRUE, 'VENDOR_OPS', NULL),
(1008, 102, 'Desserts', 3, TRUE, 'VENDOR_OPS', NULL),
(1009, 102, 'Chicken Dishes', 4, TRUE, 'VENDOR_OPS', NULL);

-- ---------------------------------------------------------
-- 103 - The Kitchen And Curry
-- Description: North Indian, Chinese, healthy meals,
-- beverages, and limited non-veg options
-- ---------------------------------------------------------
INSERT INTO menu_categories
(id, vendor_id, category_name, display_order, is_active, source_system, external_category_id)
VALUES
(1010, 103, 'North Indian', 1, TRUE, 'VENDOR_OPS', NULL),
(1011, 103, 'Chinese', 2, TRUE, 'VENDOR_OPS', NULL),
(1012, 103, 'Healthy Food', 3, TRUE, 'VENDOR_OPS', NULL),
(1013, 103, 'Beverages', 4, TRUE, 'VENDOR_OPS', NULL),
(1014, 103, 'Non-Veg Specials', 5, TRUE, 'VENDOR_OPS', NULL);

-- ---------------------------------------------------------
-- 104 - Cook House
-- Description: Homestyle North Indian food, biryani,
-- continental dishes, and beverages
-- ---------------------------------------------------------
INSERT INTO menu_categories
(id, vendor_id, category_name, display_order, is_active, source_system, external_category_id)
VALUES
(1016, 104, 'North Indian', 1, TRUE, 'VENDOR_OPS', NULL),
(1017, 104, 'Biryani & Rice', 2, TRUE, 'VENDOR_OPS', NULL),
(1018, 104, 'Continental', 3, TRUE, 'VENDOR_OPS', NULL),
(1019, 104, 'Beverages', 4, TRUE, 'VENDOR_OPS', NULL);

-- ---------------------------------------------------------
-- 105 - Tea Tradition
-- Description: Snacks, chaat, bakery items, desserts,
-- beverages, and quick bites
-- ---------------------------------------------------------
INSERT INTO menu_categories
(id, vendor_id, category_name, display_order, is_active, source_system, external_category_id)
VALUES
(1020, 105, 'Snacks & Chaat', 1, TRUE, 'VENDOR_OPS', NULL),
(1021, 105, 'Quick Bites', 2, TRUE, 'VENDOR_OPS', NULL),
(1022, 105, 'Bakery', 3, TRUE, 'VENDOR_OPS', NULL),
(1023, 105, 'Desserts', 4, TRUE, 'VENDOR_OPS', NULL),
(1024, 105, 'Beverages', 5, TRUE, 'VENDOR_OPS', NULL);

-- ---------------------------------------------------------
-- 106 - The Italian Oven
-- Description: Pizzas, continental food, fast food,
-- and light snacks
-- ---------------------------------------------------------
INSERT INTO menu_categories
(id, vendor_id, category_name, display_order, is_active, source_system, external_category_id)
VALUES
(1027, 106, 'Pizzas', 1, TRUE, 'VENDOR_OPS', NULL),
(1028, 106, 'Continental', 2, TRUE, 'VENDOR_OPS', NULL),
(1029, 106, 'Fast Food', 3, TRUE, 'VENDOR_OPS', NULL),
(1030, 106, 'Light Snacks', 4, TRUE, 'VENDOR_OPS', NULL);

-- ---------------------------------------------------------
-- 107 - The Crazy Chef
-- Description: Pizzas, Chinese, fast food, snacks,
-- and refreshing beverages
-- ---------------------------------------------------------
INSERT INTO menu_categories
(id, vendor_id, category_name, display_order, is_active, source_system, external_category_id)
VALUES
(1033, 107, 'Pizzas', 1, TRUE, 'VENDOR_OPS', NULL),
(1034, 107, 'Chinese', 2, TRUE, 'VENDOR_OPS', NULL),
(1035, 107, 'Fast Food', 3, TRUE, 'VENDOR_OPS', NULL),
(1036, 107, 'Snacks', 4, TRUE, 'VENDOR_OPS', NULL),
(1037, 107, 'Beverages', 5, TRUE, 'VENDOR_OPS', NULL);

-- ---------------------------------------------------------
-- 108 - Stardom
-- Description: Kathi rolls, fast food, pizzas,
-- and Chinese dishes
-- ---------------------------------------------------------
INSERT INTO menu_categories
(id, vendor_id, category_name, display_order, is_active, source_system, external_category_id)
VALUES
(1039, 108, 'Kathi Rolls', 1, TRUE, 'VENDOR_OPS', NULL),
(1040, 108, 'Fast Food', 2, TRUE, 'VENDOR_OPS', NULL),
(1041, 108, 'Pizzas', 3, TRUE, 'VENDOR_OPS', NULL),
(1042, 108, 'Chinese', 4, TRUE, 'VENDOR_OPS', NULL);

-- ---------------------------------------------------------
-- 109 - Dev Sweets and Snacks
-- Description: Pure veg snacks, sweets, chaat, Chinese,
-- and fast food options
-- ---------------------------------------------------------
INSERT INTO menu_categories
(id, vendor_id, category_name, display_order, is_active, source_system, external_category_id)
VALUES
(1046, 109, 'Pure Veg Snacks', 1, TRUE, 'VENDOR_OPS', NULL),
(1047, 109, 'Snacks & Chaat', 2, TRUE, 'VENDOR_OPS', NULL),
(1048, 109, 'Sweets & Desserts', 3, TRUE, 'VENDOR_OPS', NULL),
(1049, 109, 'Chinese', 4, TRUE, 'VENDOR_OPS', NULL),
(1050, 109, 'Fast Food', 5, TRUE, 'VENDOR_OPS', NULL);

-- ---------------------------------------------------------
-- 110 - Chilling Point
-- Description: Ice creams, thick shakes, cold beverages,
-- and Maggi
-- ---------------------------------------------------------
INSERT INTO menu_categories
(id, vendor_id, category_name, display_order, is_active, source_system, external_category_id)
VALUES
(1053, 110, 'Ice Creams', 1, TRUE, 'VENDOR_OPS', NULL),
(1054, 110, 'Thick Shakes', 2, TRUE, 'VENDOR_OPS', NULL),
(1055, 110, 'Cold Beverages', 3, TRUE, 'VENDOR_OPS', NULL),
(1056, 110, 'Maggi', 4, TRUE, 'VENDOR_OPS', NULL);

-- ---------------------------------------------------------
-- 111 - Tea Post
-- Description: Tea, beverages, and light snacks
-- ---------------------------------------------------------
INSERT INTO menu_categories
(id, vendor_id, category_name, display_order, is_active, source_system, external_category_id)
VALUES
(1057, 111, 'Tea', 1, TRUE, 'VENDOR_OPS', NULL),
(1058, 111, 'Beverages', 2, TRUE, 'VENDOR_OPS', NULL),
(1059, 111, 'Light Snacks', 3, TRUE, 'VENDOR_OPS', NULL);



INSERT INTO calendar_events
(id, event_date, event_type, title, description, impact_level, campus_area, vendor_id, is_active)
VALUES

-- Global events (ALL vendors affected)
(6001, '2026-04-05', 'FEST', 'Spring Hostel Carnival',
 'Heavy evening footfall expected across GHS vendors.', 
 3, 'GHS', NULL, TRUE),

(6002, '2026-04-10', 'EXAM', 'Mid-Sem Exam Week',
 'Higher demand for tea, coffee, and quick meals across hostel vendors.', 
 2, 'GHS', NULL, TRUE),

(6003, '2026-04-08', 'FEST', 'Holi Celebration',
 'Increased demand for snacks, sweets, and beverages across all vendors.', 
 3, 'GHS', NULL, TRUE),

-- Vendor-specific (based on REAL locations)

-- Chatkara (Below B1)
(6004, '2026-04-07', 'SPORTS', 'Cricket Match Screening',
 'Crowd expected near Below B1 area; higher demand at Chatkara.', 
 2, 'Below B1', 101, TRUE),

-- Pizza Bakers (Below B1)
(6005, '2026-04-07', 'SPORTS', 'Match Night Rush',
 'Increased pizza and fast food orders expected near Below B1.', 
 2, 'Below B1', 102, TRUE),

-- Tea Tradition (Below B4)
(6006, '2026-04-12', 'HOSTEL', 'Late Night Study Session',
 'Students expected near Below B4; higher demand for tea, snacks, and bakery.', 
 2, 'Below B4', 105, TRUE),

-- Dev Sweets & Snacks (Below B4)
(6007, '2026-04-08', 'FEST', 'Festival Sweets Rush',
 'Higher demand for sweets and snacks during celebrations.', 
 3, 'Below B4', 109, TRUE),

-- Chilling Point (Below B4)
(6008, '2026-04-15', 'WEATHER', 'Hot Weather Day',
 'Increased demand for ice creams, shakes, and cold beverages.', 
 2, 'Below B4', 110, TRUE),

-- Tea Post (Below G4)
(6009, '2026-04-10', 'EXAM', 'Exam Break Rush',
 'Students likely to gather for tea and snacks during exam breaks.', 
 2, 'Below G4', 111, TRUE);