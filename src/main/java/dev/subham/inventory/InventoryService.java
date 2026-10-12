package dev.subham.inventory;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventoryService {
  private final ProductRepository products;
  private final ReservationRepository reservations;

  public InventoryService(ProductRepository products,ReservationRepository reservations){
    this.products=products; this.reservations=reservations;
  }

  @Transactional
  public Reservation reserve(Long productId,int quantity,String key){
    // Lock the product first to serialize reservations for the same SKU.
    Product product = products.findLocked(productId).orElseThrow(() ->
      new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found"));
    Reservation existing=reservations.findByIdempotencyKey(key).orElse(null);
    if(existing!=null){
      if(!existing.getProductId().equals(productId)||existing.getQuantity()!=quantity){
        throw new ResponseStatusException(HttpStatus.CONFLICT,"Idempotency key reused with different payload");
      }
      return existing;
    }
    if(product.getStock()<quantity){
      throw new ResponseStatusException(HttpStatus.CONFLICT,"Insufficient stock");
    }
    product.reserve(quantity);
    return reservations.save(new Reservation(product,quantity,key));
  }

  @Transactional
  public Reservation cancel(Long reservationId){
    // Use a consistent lock ordering (product, then reservation), including cancel.
    Reservation snapshot=reservations.findById(reservationId).orElseThrow(() ->
      new ResponseStatusException(HttpStatus.NOT_FOUND,"Reservation not found"));
    Product product=products.findLocked(snapshot.getProductId()).orElseThrow();
    Reservation reservation=reservations.findLocked(reservationId).orElseThrow();
    if(!reservation.isCancelled()){
      product.release(reservation.getQuantity());
      reservation.cancel();
    }
    return reservation;
  }
}
