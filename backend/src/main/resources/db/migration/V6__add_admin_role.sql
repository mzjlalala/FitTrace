-- FitTrace Admin Role v6
-- sys_user 增加角色字段 + 种子管理员账号（admin / 123456，bcrypt）

ALTER TABLE sys_user ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
COMMENT ON COLUMN sys_user.role IS '角色（USER=普通用户/ADMIN=管理员）';

-- 管理员种子（bcrypt("123456") 已生成验证；ON CONFLICT 防重复执行）
INSERT INTO sys_user (username, password, nickname, role, status)
VALUES ('admin', '$2a$10$TEJCM0CR6HltkNpZ4URY9eWB9wJ5XCOsb0IqThGD0UpdAKAlqicw.', '管理员', 'ADMIN', 1)
ON CONFLICT (username) DO NOTHING;
