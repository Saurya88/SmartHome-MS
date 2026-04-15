-- ========================================================
-- H2 MOCK DATA (FIXED UUID FORMAT)
-- ========================================================

-- -- 1. USERS (Hex-safe: a, b, c)
-- INSERT INTO USERS (USER_ID, USERNAME, EMAIL_ADDRESS, ACCOUNT_STATUS, ROLE)
-- VALUES ('a1a1a1a1-bbbb-cccc-dddd-eeeeeeeeeeee', 'admin_pro', 'admin@homelink.io', 'ACTIVE', 'OWNER'),
--        ('b2b2b2b2-bbbb-cccc-dddd-eeeeeeeeeeee', 'j_doe', 'john.doe@example.com', 'ACTIVE', 'MEMBER'),
--        ('c3c3c3c3-bbbb-cccc-dddd-eeeeeeeeeeee', 'guest_visitor', 'guest@temp.com', 'SUSPENDED', 'GUEST');

-- 2. DEVICES (Hex-safe: d)
INSERT INTO DEVICES (DEVICE_ID, DEVICE_NAME, DEVICE_TYPE, STATUS, FIRMWARE_VERSION, CONNECTIVITY_TYPE, REGISTERED_AT)
VALUES ('d1d1d1d1-1111-2222-3333-444444444444', 'Front Door Camera', 'CAMERA', 'ONLINE', 'v4.2.1', 'WIFI',
        CURRENT_TIMESTAMP),
       ('d2d2d2d2-1111-2222-3333-444444444444', 'Living Room Light', 'LIGHT', 'ONLINE', 'v1.0.2', 'ZIGBEE',
        CURRENT_TIMESTAMP),
       ('d3d3d3d3-1111-2222-3333-444444444444', 'Main Thermostat', 'THERMOSTAT', 'MAINTENANCE', 'v2.1.0', 'ETHERNET',
        CURRENT_TIMESTAMP),
       ('d4d4d4d4-1111-2222-3333-444444444444', 'Back Door Lock', 'LOCK', 'ONLINE', 'v3.0.0', 'BLUETOOTH',
        CURRENT_TIMESTAMP),
       ('d5d5d5d5-1111-2222-3333-444444444444', 'Motion Sensor Hallway', 'SENSOR', 'OFFLINE', 'v0.9.1', 'Z-WAVE',
        CURRENT_TIMESTAMP);

-- 3. DEVICE_CAPABILITIES
INSERT INTO DEVICE_CAPABILITIES (DEVICE_ID, CAPABILITY)
VALUES ('d1d1d1d1-1111-2222-3333-444444444444', 'NIGHT_VISION'),
       ('d1d1d1d1-1111-2222-3333-444444444444', 'MOTION_DETECTION'),
       ('d2d2d2d2-1111-2222-3333-444444444444', 'DIMMABLE'),
       ('d2d2d2d2-1111-2222-3333-444444444444', 'RGB_COLOR'),
       ('d3d3d3d3-1111-2222-3333-444444444444', 'HUMIDITY_SENSING');

-- -- 4. USER_PERMISSIONS
-- INSERT INTO USER_PERMISSIONS (USER_ID, DEVICE_ID, ACCESS_LEVEL)
-- VALUES ('a1a1a1a1-bbbb-cccc-dddd-eeeeeeeeeeee', 'd1d1d1d1-1111-2222-3333-444444444444', 'FULL_ACCESS'),
--        ('a1a1a1a1-bbbb-cccc-dddd-eeeeeeeeeeee', 'd3d3d3d3-1111-2222-3333-444444444444', 'FULL_ACCESS'),
--        ('b2b2b2b2-bbbb-cccc-dddd-eeeeeeeeeeee', 'd2d2d2d2-1111-2222-3333-444444444444', 'CONTROL'),
--        ('c3c3c3c3-bbbb-cccc-dddd-eeeeeeeeeeee', 'd1d1d1d1-1111-2222-3333-444444444444', 'READ_ONLY');
--
-- -- 5. SCENES (Changed 's' to 'e' to be valid Hex)
-- INSERT INTO SCENES (SCENE_ID, NAME, STATUS, CREATED_AT)
-- VALUES ('e1e1e1e1-aaaa-bbbb-cccc-dddddddddddd', 'Night Shift', 'ACTIVE', CURRENT_TIMESTAMP),
--        ('e2e2e2e2-aaaa-bbbb-cccc-dddddddddddd', 'Energy Saver', 'SCHEDULED', CURRENT_TIMESTAMP),
--        ('e3e3e3e3-aaaa-bbbb-cccc-dddddddddddd', 'Vacation Mode', 'INACTIVE', CURRENT_TIMESTAMP);
--
-- -- 6. SCENE_DEVICE_ACTIONS
-- INSERT INTO SCENE_DEVICE_ACTIONS (SCENE_ID, DEVICE_ID, ACTION_TYPE, PARAMETERS)
-- VALUES ('e1e1e1e1-aaaa-bbbb-cccc-dddddddddddd', 'd2d2d2d2-1111-2222-3333-444444444444', 'DIM_TO', '{"brightness": 10}'),
--        ('e1e1e1e1-aaaa-bbbb-cccc-dddddddddddd', 'd4d4d4d4-1111-2222-3333-444444444444', 'LOCK_DOOR', '{"force": true}'),
--        ('e2e2e2e2-aaaa-bbbb-cccc-dddddddddddd', 'd3d3d3d3-1111-2222-3333-444444444444', 'SET_TEMP', '{"target": 18}');
--
-- -- 7. TRIGGER_RULES (IDs are strings/VARCHAR, so these are fine)
-- INSERT INTO TRIGGER_RULES (TRIGGER_ID, TRIGGER_TYPE, ENABLED, START_TIME, CONDITION_VALUE, TRIGGER_VALUE)
-- VALUES ('trig_001', 'TIME_BASED', TRUE, CURRENT_TIMESTAMP, '22:00', 'DAILY'),
--        ('trig_002', 'SENSOR_EVENT', TRUE, CURRENT_TIMESTAMP, 'MOTION_DETECTED', 'HIGH_SENSITIVITY'),
--        ('trig_003', 'MANUAL', FALSE, CURRENT_TIMESTAMP, NULL, 'USER_CLICK');
--
-- -- 8. SCENE_TRIGGER_RULES
-- INSERT INTO SCENE_TRIGGER_RULES (SCENE_ID, TRIGGER_ID)
-- VALUES ('e1e1e1e1-aaaa-bbbb-cccc-dddddddddddd', 'trig_001'),
--        ('e3e3e3e3-aaaa-bbbb-cccc-dddddddddddd', 'trig_002');
--
-- -- 9. FIRMWARE
-- INSERT INTO FIRMWARE (FIRMWARE_ID, MAJOR, MINOR, PATCH, MANDATORY, RELEASE_DATE, COMPATIBLE_DEVICE_TYPE)
-- VALUES ('f1f1f1f1-0000-0000-0000-000000000000', 4, 3, 0, TRUE, CURRENT_DATE, 'CAMERA'),
--        ('f2f2f2f2-0000-0000-0000-000000000000', 1, 1, 5, FALSE, CURRENT_DATE, 'LIGHT');