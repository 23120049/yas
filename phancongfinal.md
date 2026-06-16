# Phân Chia Công Việc & Chiến Lược Thực Hiện Project

## Đồ Án 2: Xây Dựng Hệ Thống Continuous Deployment — YAS Microservices

> **Thời gian:** 17/06/2026 → 30/06/2026
>
> **Nhóm:** A (máy 8GB) · B (máy 16GB) · C (máy 16GB) · D (máy 16GB)
>
> **Tổng RAM Cluster:** 56GB
>
> **Mô hình triển khai:** K3s Cluster + Tailscale VPN + GitHub Actions + ArgoCD + Istio

---

# Mục Tiêu

Hoàn thiện hệ thống Continuous Deployment cho ứng dụng YAS Microservices trên Kubernetes.

Yêu cầu:

* Triển khai môi trường Dev
* Triển khai môi trường Staging
* Áp dụng GitOps bằng ArgoCD
* Áp dụng Service Mesh bằng Istio
* Hoàn thành toàn bộ deliverables cơ bản và nâng cao
* Đáp ứng mục tiêu học tập của đồ án

---

# Kiến Trúc Tổng Thể

## Nguyên Tắc GitOps

GitHub Actions không triển khai trực tiếp lên Kubernetes.

GitHub Actions chỉ:

1. Build Docker Image
2. Push Docker Hub
3. Cập nhật image tag trong repository GitOps

ArgoCD là thành phần duy nhất có quyền thay đổi trạng thái cluster.

---

## Kiến Trúc CD

```text
                 ┌─────────────────────────┐
                 │       GitHub Repo       │
                 │        yas-app          │
                 └────────────┬────────────┘
                              │
                              ▼
                   GitHub Actions CI/CD
                              │
          ┌───────────────────┼───────────────────┐
          │                                       │
          ▼                                       ▼
   Docker Build                           Update values
          │                               image-tags.yaml
          ▼                                       │
      Docker Hub                                 ▼
                                          yas-gitops Repo
                                                  │
                                                  ▼
                                              ArgoCD
                                                  │
                                                  ▼
                                             K3s Cluster
```

---

## Kiến Trúc Cluster

```text
K3s Cluster (4 Nodes)

Node B (16GB)
┌────────────────────────────┐
│ Control Plane              │
│ ArgoCD                     │
│ istiod                     │
│ Kiali                      │
└────────────────────────────┘

Node C (16GB)
┌────────────────────────────┐
│ Kafka                      │
│ Kafka Connect              │
│ Debezium                   │
│ Keycloak                   │
└────────────────────────────┘

Node D (16GB)
┌────────────────────────────┐
│ PostgreSQL                 │
│ Elasticsearch              │
└────────────────────────────┘

Node A (8GB)
┌────────────────────────────┐
│ Lightweight Services       │
│ Frontend                   │
│ Utility Services           │
└────────────────────────────┘
```
---

## Mục Đích Các Repository
**yas-app**
Chứa source code của toàn bộ microservices
Chứa GitHub Actions CI/CD workflows
Build và push Docker images
  
**yas-helm**
Chứa Helm Charts
Chứa values cho Dev
Chứa values cho Staging
Định nghĩa cách triển khai ứng dụng 
  
**yas-gitops**
Chứa ArgoCD Applications
Chứa Istio manifests
Chứa Infra manifests
Là nguồn trạng thái mong muốn (Desired State) của cluster

---

# Luồng CD Chi Tiết

## Flow 1 — Deploy Dev

Trigger:

```text
Push lên nhánh main
```

Flow:

```text
Developer Push
        │
        ▼
GitHub Actions
        │
        ├─ Build Docker Image
        ├─ Push Docker Hub
        └─ Update:
           values/dev/image-tags.yaml
                │
                ▼
          GitOps Repository
                │
                ▼
             ArgoCD
                │
                ▼
        Sync Namespace Dev
                │
                ▼
        Rolling Update Pods
```

---

## Flow 2 — Deploy Staging

Trigger:

```text
Git Tag vX.Y.Z
```

Flow:

```text
Developer Create Tag
        │
        ▼
GitHub Actions
        │
        ├─ Build Docker Image
        ├─ Push Docker Hub
        └─ Update:
           values/staging/image-tags.yaml
                │
                ▼
          GitOps Repository
                │
                ▼
             ArgoCD
                │
                ▼
       Sync Namespace Staging
                │
                ▼
        Rolling Update Pods
```

---

## Flow 3 — Developer Build

Trigger:

```text
workflow_dispatch
```

Flow:

```text
Developer chọn branch cần test
            │
            ▼
GitHub Actions
            │
            ▼
Update values/dev/image-tags.yaml
            │
            ▼
GitOps Repository
            │
            ▼
ArgoCD Sync
            │
            ▼
Dev Environment Update
```

---

Flow 4 — Cleanup / Rollback

Trigger:
```
workflow_dispatch
```

Flow:
```
GitHub Actions
      │
      ▼
Chọn image tag ổn định trước đó
      │
      ▼
Update values/dev/image-tags.yaml
      │
      ▼
GitOps Repository
      │
      ▼
ArgoCD Sync
      │
      ▼
Rollback Dev Environment
```
Không sử dụng tag latest để rollback. Rollback luôn thực hiện bằng commit SHA hoặc version tag đã biết là ổn định.

---

# Phân Công Công Việc
A = Huyền
B = Hương
C = Dương
D = Ngọc
---
| Người | Tự lo được                        | Phải communicate                                 |
| ----- | --------------------------------- | ------------------------------------------------ |
| A     | GitHub Actions, Helm, GitOps      | Kafka, DB, Keycloak, Elastic endpoint            |
| B     | K3s, Namespace, Scheduling        | Cluster info cho cả nhóm                         |
| C     | Kafka, Debezium, Keycloak, ArgoCD | Lấy DB info từ D, cung cấp endpoint cho A        |
| D     | PostgreSQL, Elastic, Istio        | Cần namespace từ B, cung cấp endpoint cho A và C |

## A — GitOps & Helm Lead

### Phụ trách

* GitHub Organization
* Docker Hub Organization
* GitOps Repository
* Helm Chart toàn bộ YAS
* Values Dev/Staging
* GitHub Actions Workflows
* Deploy Dev
* Deploy Staging
* Integration Testing
* Tổng hợp báo cáo

### Timeline

| Ngày     | Task                                |
| -------- | ----------------------------------- |
| 17/06    | Tạo repo và Docker Hub Organization |
| 18–19/06 | Chuẩn hóa GitOps Repository         |
| 20–22/06 | Xây dựng Helm Chart                 |
| 22–23/06 | Xây dựng GitHub Actions Workflows   |
| 24/06    | Deploy Dev                          |
| 25–26/06 | Deploy Staging                      |
| 27/06    | End-to-End Testing                  |
| 28–29/06 | Báo cáo và Demo                     |
| 30/06    | Nộp bài                             |

---

## B — K3s Platform Lead

### Phụ trách

* K3s Cluster
* Control Plane
* Namespace
* Scheduling
* Cluster Troubleshooting

### Timeline

| Ngày     | Task                              |
| -------- | --------------------------------- |
| 17/06    | Chuẩn bị Node B                   |
| 18/06    | Dựng Control Plane                |
| 19/06    | Join Cluster                      |
| 20/06    | Tạo Namespace                     |
| 21–22/06 | Scheduling và Resource Allocation |
| 23–26/06 | Hỗ trợ triển khai workload        |
| 27–29/06 | Chuẩn bị tài liệu cluster         |
| 30/06    | Nộp bài                           |

---

## C — Kafka / Keycloak / ArgoCD Lead

### Phụ trách

* Kafka
* Kafka Connect
* Debezium
* Keycloak
* ArgoCD

### Timeline

| Ngày     | Task              |
| -------- | ----------------- |
| 18/06    | Join Cluster      |
| 19–20/06 | Kafka + Debezium  |
| 21/06    | Keycloak          |
| 22–23/06 | ArgoCD            |
| 24–26/06 | GitOps Validation |
| 27–29/06 | Báo cáo           |
| 30/06    | Nộp bài           |

---

## D — Database & Service Mesh Lead

### Phụ trách

* PostgreSQL
* Elasticsearch
* Istio
* mTLS
* AuthorizationPolicy
* Retry Policy
* Kiali

### Timeline

| Ngày     | Task                       |
| -------- | -------------------------- |
| 18/06    | Join Cluster               |
| 19–20/06 | PostgreSQL                 |
| 21/06    | Elasticsearch              |
| 22/06    | Istio                      |
| 23–24/06 | mTLS + AuthorizationPolicy |
| 25/06    | Retry Policy               |
| 26–29/06 | Kiali + Evidence + Report  |
| 30/06    | Nộp bài                    |

---

# Checklist Hoàn Thành

## Cluster

* [ ] 4 Node Ready
* [ ] Namespace Dev
* [ ] Namespace Staging
* [ ] Namespace Infra
* [ ] Tailscale hoạt động

## Infra

* [ ] PostgreSQL
* [ ] Elasticsearch
* [ ] Kafka
* [ ] Kafka Connect
* [ ] Debezium
* [ ] Keycloak

## GitOps

* [ ] GitOps

* [ ] ArgoCD

* [ ] Dev Auto Sync

* [ ] Staging Sync

* [ ] GitHub Actions cập nhật values file

* [ ] GitHub Actions tạo image tag theo commit SHA

* [ ] Helm values sử dụng image tag bất biến (immutable tag)
## Application

* [ ] Helm Chart hoàn chỉnh
* [ ] Dev Running
* [ ] Staging Running
* [ ] Không có CrashLoopBackOff

## Service Mesh

* [ ] Istio
* [ ] mTLS
* [ ] AuthorizationPolicy
* [ ] Retry Policy
* [ ] Kiali

## Demo

* [ ] GitOps Demo
* [ ] CDC Demo
* [ ] Keycloak Demo
* [ ] mTLS Demo
* [ ] Retry Demo
* [ ] Dev/Staging Demo

## Báo Cáo

* [ ] Screenshot đầy đủ
* [ ] README hoàn chỉnh
* [ ] Báo cáo hoàn chỉnh

---

# Checklist Bảo Vệ

* [ ] Chứng minh Git Commit → ArgoCD Sync
* [ ] Chứng minh Dev và Staging độc lập
* [ ] Chứng minh PostgreSQL → Debezium → Kafka CDC
* [ ] Chứng minh Keycloak hoạt động
* [ ] Chứng minh mTLS đang bật
* [ ] Chứng minh AuthorizationPolicy chặn truy cập
* [ ] Chứng minh Retry Policy hoạt động
* [ ] Chứng minh Kiali hiển thị topology
* [ ] Chứng minh Deploy Dev thành công
* [ ] Chứng minh Deploy Staging thành công

---

# Mốc Hoàn Thành

* [ ] 19/06 — 4 Node Ready
* [ ] 22/06 — Infra hoàn chỉnh
* [ ] 23/06 — ArgoCD hoạt động
* [ ] 24/06 — Dev hoàn chỉnh
* [ ] 26/06 — Staging hoàn chỉnh
* [ ] 27/06 — Test toàn bộ deliverables
* [ ] 28/06 — Demo nội bộ
* [ ] 29/06 — Khóa báo cáo
* [ ] 30/06 — Nộp bài

```
```
