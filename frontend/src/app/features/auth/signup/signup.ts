import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../../core/services/auth';
import { NotificationService } from '../../../core/services/notification';
import { MatIconModule } from '@angular/material/icon';
@Component({
  selector: 'app-signup',
  imports: [CommonModule, FormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule, MatProgressSpinnerModule, MatIconModule],
  templateUrl: './signup.html',
  styleUrl: './signup.scss'
})
export class Signup {
  email = '';
  password = '';
  confirmPassword = '';
  loading = signal(false);

  constructor(
    private authService: AuthService,
    private notification: NotificationService,
    private router: Router
  ) {}

  onSignup(): void {
    if (!this.email || !this.password) {
      this.notification.error('Email and password are required.');
      return;
    }

    if (this.password.length < 6) {
      this.notification.error('Password must be at least 6 characters.');
      return;
    }

    if (this.password !== this.confirmPassword) {
      this.notification.error('Passwords do not match.');
      return;
    }

    this.loading.set(true);

    this.authService.signup(this.email, this.password).subscribe({
      next: (response) => {
        this.loading.set(false);
        this.notification.success(response.message || 'Account created! Please check your email.');
        this.router.navigate(['/login'], { state: { justSignedUp: true } });
      },
      error: (err) => {
        this.loading.set(false);
        const message = err.error?.error || 'Signup failed. That email might already be registered.';
        this.notification.error(message);
      }
    });

  }
  hidePassword = signal(true);
hideConfirmPassword = signal(true);

togglePasswordVisibility(): void {
  this.hidePassword.set(!this.hidePassword());
}

toggleConfirmPasswordVisibility(): void {
  this.hideConfirmPassword.set(!this.hideConfirmPassword());
}
}