# Supplier Category Management - Frontend Developer Guide

## 🎯 Overview

The supplier system uses **two-level category management**, exactly like the contracting company specialties pattern.

---

## 📊 Two-Level Category System

### Level 1: Master Supplier Categories (What They Offer)

**Entity:** `SupplierCategory`  
**Purpose:** Defines what categories a supplier serves globally (for all companies)

**Example:**
```
Bunnings Warehouse offers:
✓ General Hardware
✓ Plumbing Materials  
✓ Electrical Components
✓ Paint & Finishes
✓ Timber Framing
... (30+ categories)
```

### Level 2: Company Preferred Categories (What You Prefer)

**Entity:** `CompanySupplierCategory`  
**Purpose:** Defines which categories your company prefers from this supplier

**Example:**
```
ABC Builders prefers from Bunnings:
⭐ General Hardware (primary)
⭐ Paint & Finishes (primary)
   Plumbing Materials (available but we prefer Reece)
   Electrical Components (available but we prefer Haymans)
```

---

## 🔄 API Integration Flow

### Flow 1: Creating a New Supplier

```typescript
// STEP 1: Fetch all available categories (55 Australian construction categories)
const categoriesResponse = await fetch('/api/consumable-categories', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const { data: allCategories } = await categoriesResponse.json();

// STEP 2: Display categories in multi-select component
// User selects which categories this supplier serves
const categoryMultiSelect = allCategories.map(cat => ({
  value: cat.id,
  label: cat.name,
  description: cat.description,
  displayOrder: cat.displayOrder
}));

// STEP 3: User submits form with selected categories
const selectedCategoryIds = [
  "403c1ca1-ddd6-4b64-b802-3c237e19ca35",  // General Hardware
  "0de71499-e8ab-4526-848d-8197b7a4b73f",  // Plumbing Materials
  "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d"   // Electrical Components
];

// STEP 4: Create supplier
const createSupplierResponse = await fetch('/api/suppliers', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  },
  body: JSON.stringify({
    name: "Bunnings Warehouse - Alexandria",
    address: "75 O'Riordan St, Alexandria NSW 2015",
    abn: "63000000001",
    email: "trade.alexandria@bunnings.com.au",
    phone: "(02) 9698 9800",
    supplierType: "RETAIL",
    defaultPaymentTerms: "NET_30",
    categories: selectedCategoryIds  // ← Categories they offer
  })
});

// STEP 5: Backend automatically creates SupplierCategory entries
// Result: Bunnings now linked to 3 categories in master database
```

### Flow 2: Creating Company Relationship

```typescript
// STEP 1: User selects a supplier (from master list)
const supplierId = "bunnings-uuid";

// STEP 2: Fetch supplier details to see what categories they offer
const supplierResponse = await fetch(`/api/suppliers/${supplierId}`);
const { data: supplier } = await supplierResponse.json();

// supplier.categories shows all categories Bunnings offers:
// [
//   { categoryId: "...", categoryName: "General Hardware", isPrimaryCategory: true },
//   { categoryId: "...", categoryName: "Plumbing Materials", isPrimaryCategory: false },
//   { categoryId: "...", categoryName: "Electrical Components", isPrimaryCategory: false }
// ]

// STEP 3: User selects which categories they prefer from this supplier
// Show the supplier's categories and let user pick preferred ones
const preferredCategoryIds = [
  "403c1ca1-ddd6-4b64-b802-3c237e19ca35",  // General Hardware
  "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d"   // Electrical Components
  // NOT Plumbing (we prefer Reece for plumbing)
];

// STEP 4: Create relationship
const createRelationshipResponse = await fetch(
  `/api/companies/${companyId}/suppliers/${supplierId}/relationship`,
  {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({
      preferred: true,
      accountNumber: "TRADE-12345",
      paymentTerms: "NET_30",
      creditLimit: 50000.00,
      discountRate: 10.5,
      rating: 5,
      preferredCategories: preferredCategoryIds  // ← What we prefer
    })
  }
);

// STEP 5: Backend automatically creates CompanySupplierCategory entries
// Result: ABC Builders → Bunnings relationship has 2 preferred categories
```

---

## 🎨 UI Component Recommendations

### 1. Create Supplier Form

**Category Selection Section:**
```
┌─────────────────────────────────────────────┐
│ Categories This Supplier Serves             │
├─────────────────────────────────────────────┤
│ [Search categories...]                      │
│                                              │
│ Selected Categories:                         │
│ ☑ General Hardware                          │
│ ☑ Plumbing Materials                        │
│ ☑ Electrical Components                     │
│ ☐ Bathroom Fixtures                         │
│ ☐ Kitchen Fixtures                          │
│ ...                                          │
│                                              │
│ [3 categories selected]                      │
└─────────────────────────────────────────────┘
```

**Implementation:**
- Multi-select checkbox list
- Search/filter functionality
- Display order by `displayOrder` field
- Show description on hover

### 2. Create Relationship Form

**Preferred Category Selection:**
```
┌─────────────────────────────────────────────┐
│ Supplier: Bunnings Warehouse - Alexandria   │
│ Offers: 30 categories                        │
├─────────────────────────────────────────────┤
│ Your Preferred Categories:                   │
│                                              │
│ ⭐ General Hardware                          │
│ ⭐ Electrical Components                     │
│ ☐ Plumbing Materials (available)            │
│ ☐ Paint & Finishes (available)              │
│                                              │
│ [2 preferred categories]                     │
└─────────────────────────────────────────────┘
```

**Implementation:**
- Show only categories the supplier offers
- Let user select their preferred ones
- Star icon for preferred
- Gray out non-preferred but still show them

### 3. Supplier Card Display

```
┌────────────────────────────────────────┐
│ 🏪 Bunnings Warehouse - Alexandria    │
│ Type: RETAIL                           │
│ Rating: ⭐⭐⭐⭐⭐ (5/5)                  │
├────────────────────────────────────────┤
│ Account: TRADE-12345                   │
│ Discount: 10.5%                        │
│ Terms: NET_30                          │
├────────────────────────────────────────┤
│ Serves 30 categories:                  │
│ ⭐ General Hardware (preferred)        │
│ ⭐ Electrical (preferred)              │
│ • Plumbing Materials                   │
│ • Paint & Finishes                     │
│ + 26 more...                           │
└────────────────────────────────────────┘
```

---

## 🔍 Smart Supplier Selection

### Scenario: Creating a Project Step Requirement

**User Action:** Creating requirement for "Bathroom Finishing" step

```typescript
// STEP 1: User selects category "Bathroom Fixtures"
const categoryId = "bathroom-fixtures-uuid";

// STEP 2: Frontend calls smart selection endpoint
const response = await fetch(
  `/api/companies/${companyId}/suppliers/by-category/${categoryId}`,
  { headers: { 'Authorization': `Bearer ${token}` } }
);
const { data: suppliers } = await response.json();

// STEP 3: Backend returns suppliers ordered by:
// 1. Preferred suppliers first (where this category is in preferredCategories)
// 2. Then by rating (highest first)

// Result:
[
  {
    supplierName: "Reece Plumbing - Auburn",
    preferred: true,              // ← Bathroom Fixtures is in preferred
    rating: 5,
    discountRate: 15.0,
    accountNumber: "PLM-98765",
    preferredCategories: [
      { categoryName: "Bathroom Fixtures", isPrimaryCategory: true }
    ]
  },
  {
    supplierName: "Rogerseller - Alexandria",
    preferred: false,             // ← Not in preferred categories
    rating: 4,
    discountRate: 12.0,
    accountNumber: "RG-55555"
  }
]

// STEP 4: Display suppliers with visual indicators
// Reece appears first with ⭐ badge and "Preferred" label
// Auto-fills account PLM-98765 when selected
// Shows "15% discount will be applied"
```

---

## 📋 Category Endpoints Summary

### Get All Categories
```http
GET /api/consumable-categories
→ Returns 55 Australian construction categories
→ Use for: Multi-select dropdowns, category pickers
```

### Search Categories
```http
GET /api/consumable-categories/search?searchText=plumbing
→ Returns categories matching "plumbing"
→ Use for: Autocomplete, filtered multi-select
```

### Get Category by ID
```http
GET /api/consumable-categories/{categoryId}
→ Returns single category details
→ Use for: Displaying category info, validation
```

---

## ✅ Implementation Checklist

### Supplier Creation UI
- [ ] Fetch all categories on component mount
- [ ] Display categories in multi-select (sorted by displayOrder)
- [ ] Allow search/filter of categories
- [ ] Pass selected category IDs in `categories` array
- [ ] Show success message with created supplier and categories

### Relationship Creation UI
- [ ] Fetch supplier details to get offered categories
- [ ] Display supplier's categories as options
- [ ] Let user select preferred categories (subset)
- [ ] Pass selected IDs in `preferredCategories` array
- [ ] Show relationship with preferred categories highlighted

### Supplier Selection UI (for Requirements)
- [ ] Use `/by-category/` endpoint for smart selection
- [ ] Show preferred suppliers first with ⭐ badge
- [ ] Display rating stars (1-5)
- [ ] Show discount percentage
- [ ] Auto-fill account number when supplier selected
- [ ] Apply discount to estimated cost calculation

### Display/View UI
- [ ] Show supplier's offered categories
- [ ] Highlight company's preferred categories
- [ ] Display category counts (e.g., "Serves 30 categories, 5 preferred")
- [ ] Group suppliers by preferred categories

---

## 🎯 Key Differences from Contracting Companies

| Aspect | Contracting Companies | Suppliers |
|--------|----------------------|-----------|
| **Master Entity** | Specialty | ConsumableCategory |
| **Master List Size** | ~20-30 specialties | 55 construction categories |
| **Master Relationship** | ContractingCompanySpecialty | SupplierCategory |
| **Company Relationship** | BuilderContractorSpecialty | CompanySupplierCategory |
| **Selection Context** | Crew assignments | Material ordering |
| **Primary Use** | Finding right tradie | Finding right supplier |

**Same Pattern, Different Context!** ✅

---

## 💡 Pro Tips

1. **Category Loading:** Load categories once and cache in state/context
2. **Display Order:** Always sort by `displayOrder` field (1-55)
3. **Search:** Implement client-side search for better UX if <100 categories
4. **Icons:** Icon field is available for future enhancement
5. **Grouping:** Consider grouping categories by construction phase (Foundation, Framing, etc.)
6. **Validation:** Ensure selected categories exist before submitting
7. **Feedback:** Show count of selected categories ("3 categories selected")

---

## 📱 Mobile Considerations

### Category Selection on Mobile
- Use searchable dropdown instead of checkboxes
- Show selected count badge
- Implement "Recently Used" categories
- Consider category groups/sections

### Supplier Card on Mobile
- Collapse category list by default
- Show "Serves X categories, Y preferred"
- Tap to expand category list
- Swipe actions for quick rating

---

**This guide covers everything your frontend developer needs to implement category management for suppliers!** 🎉

