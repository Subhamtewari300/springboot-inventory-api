package dev.subham.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class InventoryController {
  private final ProductRepository products;
  private final InventoryService service;
  public InventoryController(ProductRepository products,InventoryService service){this.products=products;this.service=service;}

  public record NewProduct(@NotBlank String sku,@NotBlank String name,@Min(0) int stock){}
  public record ProductDto(Long id,String sku,String name,int stock){
    static ProductDto from(Product p){return new ProductDto(p.getId(),p.getSku(),p.getName(),p.getStock());}
  }
  public record NewReservation(@NotNull Long productId,@Min(1) int quantity,@NotBlank String idempotencyKey){}
  public record ReservationDto(Long id,Long productId,int quantity,String idempotencyKey,boolean cancelled){
    static ReservationDto from(Reservation r){return new ReservationDto(r.getId(),r.getProductId(),r.getQuantity(),r.getIdempotencyKey(),r.isCancelled());}
  }

  @PostMapping("/products") @ResponseStatus(HttpStatus.CREATED)
  public ProductDto add(@Valid @RequestBody NewProduct request){
    return ProductDto.from(products.save(new Product(request.sku(),request.name(),request.stock())));
  }
  @GetMapping("/products/{id}")
  public ProductDto get(@PathVariable Long id){
    return ProductDto.from(products.findById(id).orElseThrow(() ->
      new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found")));
  }
  @PostMapping("/reservations") @ResponseStatus(HttpStatus.CREATED)
  public ReservationDto reserve(@Valid @RequestBody NewReservation request){
    return ReservationDto.from(service.reserve(request.productId(),request.quantity(),request.idempotencyKey()));
  }
  @DeleteMapping("/reservations/{id}")
  public ReservationDto cancel(@PathVariable Long id){return ReservationDto.from(service.cancel(id));}
}
