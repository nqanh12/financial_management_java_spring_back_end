-- Danh mục mẫu thực tế (tiếng Việt) cho user seed admin — chỉ insert khi user seed tồn tại.
INSERT INTO categories (id, user_id, name, type, created_at, updated_at)
SELECT t.id,
       u.id,
       t.name,
       t.type,
       now(),
       now()
FROM (VALUES
          ('10000000-0000-4000-8000-000000000001'::uuid, 'Lương & thu nhập cố định', 'INCOME'),
          ('10000000-0000-4000-8000-000000000002'::uuid, 'Thưởng & phụ cấp', 'INCOME'),
          ('10000000-0000-4000-8000-000000000003'::uuid, 'Freelance & công việc tự do', 'INCOME'),
          ('10000000-0000-4000-8000-000000000004'::uuid, 'Đầu tư, cổ tức & lãi tiết kiệm', 'INCOME'),
          ('10000000-0000-4000-8000-000000000005'::uuid, 'Quà tặng & tiền mừng', 'INCOME'),
          ('10000000-0000-4000-8000-000000000006'::uuid, 'Hoàn tiền & cashback', 'INCOME'),
          ('10000000-0000-4000-8000-000000000007'::uuid, 'Chợ & siêu thị', 'EXPENSE'),
          ('10000000-0000-4000-8000-000000000008'::uuid, 'Ăn ngoài & nhà hàng', 'EXPENSE'),
          ('10000000-0000-4000-8000-000000000009'::uuid, 'Cà phê & ăn vặt', 'EXPENSE'),
          ('10000000-0000-4000-8000-00000000000a'::uuid, 'Giao thông (Grab, taxi, vé)', 'EXPENSE'),
          ('10000000-0000-4000-8000-00000000000b'::uuid, 'Xăng dầu & gửi xe', 'EXPENSE'),
          ('10000000-0000-4000-8000-00000000000c'::uuid, 'Điện & nước', 'EXPENSE'),
          ('10000000-0000-4000-8000-00000000000d'::uuid, 'Internet & truyền hình', 'EXPENSE'),
          ('10000000-0000-4000-8000-00000000000e'::uuid, 'Điện thoại di động', 'EXPENSE'),
          ('10000000-0000-4000-8000-00000000000f'::uuid, 'Thuê nhà & trả lãi vay', 'EXPENSE'),
          ('10000000-0000-4000-8000-000000000010'::uuid, 'Sức khỏe & khám chữa bệnh', 'EXPENSE'),
          ('10000000-0000-4000-8000-000000000011'::uuid, 'Thuốc & thực phẩm chức năng', 'EXPENSE'),
          ('10000000-0000-4000-8000-000000000012'::uuid, 'Học phí & sách vở', 'EXPENSE'),
          ('10000000-0000-4000-8000-000000000013'::uuid, 'Khóa học & đào tạo', 'EXPENSE'),
          ('10000000-0000-4000-8000-000000000014'::uuid, 'Giải trí & du lịch', 'EXPENSE'),
          ('10000000-0000-4000-8000-000000000015'::uuid, 'Mua sắm quần áo & giày', 'EXPENSE'),
          ('10000000-0000-4000-8000-000000000016'::uuid, 'Làm đẹp & gym', 'EXPENSE'),
          ('10000000-0000-4000-8000-000000000017'::uuid, 'Quà biếu & hiếu hỷ', 'EXPENSE'),
          ('10000000-0000-4000-8000-000000000018'::uuid, 'Bảo hiểm & phí định kỳ', 'EXPENSE'),
          ('10000000-0000-4000-8000-000000000019'::uuid, 'Sửa chữa nhà & đồ gia dụng', 'EXPENSE')
     ) AS t(id, name, type)
         CROSS JOIN (SELECT id FROM users WHERE id = '00000000-0000-0000-0000-000000000001' LIMIT 1) u;
