# Initial User Request — EQUIP-001

## Original Request

> Extend the current
`com.github.jenkaby.bikerental.equipment.web.query.controller.EquipmentQueryController#searchEquipments`
> endpoint to provide the ability to search equipment by a search text field. This field is used to search in the
> `uid`, `serialNumber`, or `model` columns of the equipment datatable.
>
> Update the
`com.github.jenkaby.bikerental.equipment.infrastructure.persistence.adapter.EquipmentRepositoryAdapter#findAll`
> method to use `net.kaczmarzyk.spring.data.jpa.web.annotation.Spec` annotation for search functionality. Create a new
> `EquipmentSpec` interface to be able to search by `uid`, `serialNumber`, `model` for the new search input and support
> existing `type` and `status` filters.

## Clarifications Captured

| Question                                                                        | Answer                                                                              |
|---------------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| Match type                                                                      | Contains — case-insensitive substring (LIKE %text%)                                 |
| Case sensitivity                                                                | Case-insensitive                                                                    |
| API parameter name                                                              | `q`                                                                                 |
| Existing unused DTO fields (`uid`, `serialNumber` in `SearchEquipmentsRequest`) | Retire — remove them                                                                |
| Architecture for `EquipmentSpec`                                                | Infrastructure layer, following the `CustomerSpec` / `SpecificationBuilder` pattern |
