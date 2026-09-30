import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../../core/services/auth';
import { NotificationService } from '../../../core/services/notification';
import { MatIconModule } from '@angular/material/icon';
@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule, RouterLink, MatFormFieldModule, MatInputModule, MatButtonModule,MatIconModule],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {
  email = '';
  password = '';
  showVerifyBanner = signal(false);

  constructor(
    private authService: AuthService,
    private notification: NotificationService,
    private router: Router
  ) {
    const navigation = this.router.getCurrentNavigation();
    const state = navigation?.extras?.state as { justSignedUp?: boolean } | undefined;
    if (state?.justSignedUp) {
      this.showVerifyBanner.set(true);
    }
  }

  onLogin(): void {
    if (!this.email || !this.password) {
      this.notification.error('Email and password are required.');
      return;
    }

    this.authService.login(this.email, this.password).subscribe({
      next: () => {
        this.notification.success('Welcome back!');
        this.router.navigate(['/']);
      },
      error: (err) => {
        const message = err.error?.error || 'Invalid email or password.';
        this.notification.error(message);
      }
    });
  }
  hidePassword = signal(true);

togglePasswordVisibility(): void {
  this.hidePassword.set(!this.hidePassword());
}
}