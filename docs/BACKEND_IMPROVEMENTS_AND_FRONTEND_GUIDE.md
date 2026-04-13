# Cải tiến backend & hướng dẫn tích hợp cho Frontend

Tài liệu này gồm hai phần:

1. **Prompt / backlog** dùng cho developer hoặc AI agent triển khai các sửa đổi backend theo review kiến trúc.
2. **Hướng dẫn cho Frontend** — hợp đồng API màn **Ví tiền** (tổng quan + từng ví) và các field ví mở rộng; các mục khác (rate limit, phân trang, …) vẫn tham chiếu Phần A khi backend triển khai.

---

## Phần A — Prompt triển khai backend (copy vào ticket hoặc agent)

Bạn là developer Spring Boot 3 làm việc trên repo `financial_management_java_spring_back_end`. Thực hiện các hạng mục sau, ưu tiên theo thứ tự P0 → P1 → P2. Giữ diff tối thiểu, không refactor lan man.

### P0 — Bảo mật & cấu hình

1. **Loại bỏ secret thật khỏi `application.yaml`**: Không đặt default cho `GOOGLE_CLIENT_SECRET`, `GOOGLE_CLIENT_ID` thật, hay `JWT_SECRET`. Chỉ dùng placeholder hoặc để trống bắt buộc override bằng biến môi trường. Thêm `@ConfigurationProperties` hoặc `EnvironmentPostProcessor` / `ApplicationListener` để **fail fast** khi profile `prod` thiếu các biến bắt buộc.
2. Ghi chú trong README: rotate credential nếu từng commit lộ.

### P0 — Rate limiting sản phẩm thật

1. Thay `RateLimitFilter` in-memory bằng giới hạn dựa trên **Redis** (đã có dependency Redis), ví dụ token bucket hoặc fixed window theo key `userId` (nếu đã auth) hoặc `clientIp` đã chuẩn hoá.
2. Nếu chạy sau reverse proxy: cấu hình Spring `ForwardedHeaderFilter` / `server.forward-headers-strategy` phù hợp và chỉ tin `X-Forwarded-For` từ proxy tin cậy.
3. Trả response **429** kèm body JSON thống nhất với `RestExceptionHandler` (xem P1) và header **`Retry-After`** (giây) khi có thể.

### P1 — Phân trang API đọc danh sách

1. Thêm phân trang cho `GET /api/v1/transactions` (và các list lớn khác nếu cần: ví dụ `budgets`, `budget-alerts`): query `page` (0-based), `size` (max ví dụ 100), optional `sort`.
2. Response dùng `Page<TransactionResponse>` hoặc DTO bọc rõ: `{ "content": [...], "page": 0, "size": 20, "totalElements": N, "totalPages": M }` để frontend không phụ thuộc Spring Data trần nếu muốn ổn định contract.
3. Giữ endpoint cũ không breaking: nếu không thể, version path `v2` hoặc tham số `?unpaged=true` tạm thời (không khuyến nghị lâu dài).

### P1 — Budget alerts idempotent

1. Thêm unique constraint `(user_id, category_id, year_month)` trên `budget_alerts` (chỉ bản ghi “active” — nếu dùng soft delete thì partial unique `WHERE deleted_at IS NULL` trên PostgreSQL).
2. Đổi `saveAlert` thành **upsert**: cập nhật `spent_amount`, `limit_amount`, `message`, `created_at` hoặc thêm `updated_at` nếu cần; không tạo hàng nghìn bản ghi trùng.
3. Migration Flyway mới, không sửa migration cũ.

### P1 — Exception handling thống nhất

1. Mở rộng `RestExceptionHandler`: `AccessDeniedException` → 403, `AuthenticationException` / `BadCredentialsException` → 401 với body cùng format `{ timestamp, status, error, message }`.
2. Optional: thêm `@ExceptionHandler(Exception.class)` trả 500 với message generic, log stack server-side.
3. JWT filter: không nuốt lỗi hoàn toàn — log WARN có giới hạn tần suất hoặc DEBUG cho parse fail.

### P2 — Observability

1. Structured logging (JSON) + `traceId`/`spanId` (Micrometer Tracing hoặc Spring Boot 3 observability).
2. Actuator: cân nhắc expose `metrics` trên port/management riêng, bảo vệ network.

### P2 — JWT & phiên (tùy roadmap)

1. Nếu access token TTL dài: cân nhắc refresh token (httpOnly cookie hoặc endpoint rotate) — chỉ làm khi product yêu cầu.

---

## Phần B — Ghi chú cho Frontend (màn Ví tiền & hợp đồng ví)

### B.1. Tổng quan màn Ví — `GET /api/v1/wallets/overview`

- **Auth**: giống các API ví khác — header `Authorization: Bearer <access_token>`.
- **Mục đích**: một request cho phần header (tài sản / nợ / ròng theo từng loại tiền) + danh sách ví **nhóm theo** `groupKey`, kèm tổng trong từng nhóm.

**Cấu trúc response**

```json
{
  "summary": [
    {
      "currency": "VND",
      "totalAssets": "4357000",
      "totalDebts": "0",
      "netAssets": "4357000"
    }
  ],
  "groups": [
    {
      "groupKey": "CASH",
      "groupSummary": [
        {
          "currency": "VND",
          "totalAssets": "4357000",
          "totalDebts": "0",
          "netAssets": "4357000"
        }
      ],
      "wallets": [
        {
          "id": "uuid",
          "name": "Nguyen Quoc Anh",
          "currency": "VND",
          "currentBalance": "4357000",
          "openingBalance": "0",
          "groupKey": "CASH",
          "iconKey": null,
          "displayExchangeRate": "1",
          "createdAt": "2026-04-12T10:00:00Z"
        }
      ]
    }
  ]
}
```

**Ý nghĩa field**

| Field | Ý nghĩa |
|--------|---------|
| `summary` | Mảng **theo từng `currency`**: tổng trên **mọi** ví của user. |
| `totalAssets` | Tổng các số dư **dương** (tài sản). |
| `totalDebts` | Tổng phần **âm** của số dư (hiển thị dạng số dương — “món nợ”). |
| `netAssets` | `totalAssets - totalDebts` (= tổng số dư thực theo currency). |
| `groups` | Mỗi phần tử = một `groupKey` (ví dụ `CASH`), có `groupSummary` (cùng cấu trúc `summary` nhưng chỉ trong nhóm) + `wallets`. |
| `displayExchangeRate` | Hiện luôn `1`; dành cho sau này nếu có quy đổi ngoại tệ trên API. |
| `iconKey` | Optional — client map sang asset icon (ảnh / font icon). |

**Đa tiền tệ**: mỗi phần tử trong `summary` / `groupSummary` là một `currency` riêng. UI chỉ hiển thị một dòng “tài sản ròng” khi user chọn **một** currency làm hiển thị, hoặc hiển thị nhiều dòng.

**i18n tiêu đề nhóm**: API trả **`groupKey` dạng key ổn định** (mặc định `CASH`). Client map ví dụ: `CASH` → “Tiền mặt”, `BANK` → “Ngân hàng”, …

### B.2. CRUD ví — `GET/POST/PUT/DELETE /api/v1/wallets`

**`WalletResponse`** (list, get một ví, create, update) gồm:

- `id`, `name`, `currency`, `description`, `openingBalance`, `currentBalance`, `groupKey`, `iconKey`, `createdAt`, `updatedAt`.
- `currentBalance` = `openingBalance + Σ(IN) − Σ(OUT)` (giao dịch chưa xoá mềm).

**`POST` / `PUT` body** (các field optional ngoài quy tắc validation):

- `name` (bắt buộc), `currency` (3 ký tự, mặc định logic server nếu thiếu), `description`, `openingBalance`.
- `groupKey` (optional, tối đa 64 ký tự): nếu không gửi khi tạo → server dùng `CASH`; giá trị được chuẩn hoá (trim, uppercase).
- `iconKey` (optional, tối đa 128 ký tự): có thể `null` / bỏ trống.

**FE**

- Màn danh sách / chi tiết ví: có thể dùng **`GET /api/v1/wallets`** hoặc lấy danh sách từ `overview.groups[].wallets` nếu chỉ phục vụ màn Ví.
- Màn tổng quan (header + section theo nhóm): ưu tiên **`GET /api/v1/wallets/overview`**.

### B.3. Giao dịch, lỗi, CORS (tóm tắt)

- **Giao dịch**: `note` (tối đa 2000), `externalReference` (tối đa 128) — có trong create/update và khi list/get transaction.
- **401 / 403 / 500**: body lỗi chuẩn `{ timestamp, status, error, message }` — 401 → login lại; 403 → không quyền.
- **CORS / base URL**: `APP_CORS_ORIGINS`, URL API, OAuth callback theo môi trường; local xem `application-local.yaml` (gitignored).

Các hạng mục **rate limit 429**, **phân trang** transactions/budgets/alerts — khi backend đã làm theo Phần A, FE tham chiếu ticket/PR tương ứng (không lặp chi tiết ở đây).

---

## Checklist nhanh Frontend

| Hạng mục | Hành động |
|----------|-----------|
| Màn Ví | `GET /api/v1/wallets/overview` — `summary` + `groups`; map `groupKey` → nhãn tiếng Việt trên client |
| CRUD ví | `groupKey`, `iconKey`; `currentBalance` / `openingBalance` / `description` |
| GET `/wallets` vs `/wallets/overview` | Overview = đủ dữ liệu màn tổng quan; list CRUD = endpoint cũ |
| Transactions | `note` + optional `externalReference` |
| 401 / 403 / 500 | Phân biệt login lại vs forbidden vs lỗi chung |
| 429 / phân trang | Theo Phần A khi đã triển khai — xem PR |
| API docs | Swagger dev/staging; prod có thể tắt UI |

---

## Liên hệ / đồng bộ

Khi backend merge từng PR, ghi rõ trong PR: **breaking hay không**, sample request/response, và version API nếu dùng `/api/v2/...`.
