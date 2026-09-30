import { Injectable } from '@angular/core';
import Swal from 'sweetalert2';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  success(message: string): void {
    Swal.fire({
      toast: true,
      position: 'bottom',
      icon: 'success',
      title: message,
      showConfirmButton: false,
      timer: 2500,
      timerProgressBar: true,
      background: '#fdf6f4',
      color: '#7a5a52',
      iconColor: '#d9a8a0'
    });
  }

  error(message: string): void {
    Swal.fire({
      toast: true,
      position: 'bottom',
      icon: 'error',
      title: message,
      showConfirmButton: false,
      timer: 3000,
      timerProgressBar: true,
      background: '#fdf6f4',
      color: '#7a5a52'
    });
  }

  async confirmDelete(companyName: string): Promise<boolean> {
    const result = await Swal.fire({
      title: `Delete ${companyName}?`,
      text: "This can't be undone!",
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#d9a8a0',
      cancelButtonColor: '#f0e4e0',
      confirmButtonText: 'Yes, delete it',
      background: '#fdf6f4',
      color: '#7a5a52'
    });
    return result.isConfirmed;
  }
}