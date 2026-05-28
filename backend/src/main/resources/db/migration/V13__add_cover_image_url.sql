ALTER TABLE vendors
ADD COLUMN cover_image_url VARCHAR(500);

UPDATE vendors
SET cover_image_url = 'http://localhost:8081/images/vendors/Chatkara.png'
WHERE id = 101;

UPDATE vendors
SET cover_image_url = CASE vendor_code
    WHEN 'PIZZA_BAKERS' THEN 'http://localhost:8081/images/vendors/PizzaBakers.png'
    WHEN 'KITCHEN_CURRY' THEN 'http://localhost:8081/images/vendors/KitchenAndCurry.png'
    WHEN 'COOK_HOUSE' THEN 'http://localhost:8081/images/vendors/CookHouse.png'
    WHEN 'TEA_TRADITION' THEN 'http://localhost:8081/images/vendors/TeaTradition.png'
    WHEN 'ITALIAN_OVEN' THEN 'http://localhost:8081/images/vendors/ItalianOven.png'
    WHEN 'CRAZY_CHEF' THEN 'http://localhost:8081/images/vendors/CrazyChef.png'
    WHEN 'STARDOM' THEN 'http://localhost:8081/images/vendors/Stardom.png'
    WHEN 'DEV_SWEETS_SNACKS' THEN 'http://localhost:8081/images/vendors/DevSweetsSnacks.png'
    WHEN 'CHILLING_POINT' THEN 'http://localhost:8081/images/vendors/ChillingPoint.png'
    WHEN 'TEA_POST' THEN 'http://localhost:8081/images/vendors/TeaPost.png'
    ELSE cover_image_url
END
WHERE vendor_code IN (
    'PIZZA_BAKERS',
    'KITCHEN_CURRY',
    'COOK_HOUSE',
    'TEA_TRADITION',
    'ITALIAN_OVEN',
    'CRAZY_CHEF',
    'STARDOM',
    'DEV_SWEETS_SNACKS',
    'CHILLING_POINT',
    'TEA_POST'
);