import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-verify-email',
  imports: [CommonModule, RouterLink, MatButtonModule],
  templateUrl: './verify-email.html',
  styleUrl: './verify-email.scss'
})
export class VerifyEmail implements OnInit {
  status = signal<'loading' | 'success' | 'error'>('loading');
  message = signal('');

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.status.set('error');
      this.message.set('No verification token found.');
      return;
    }

    this.authService.verifyEmail(token).subscribe({
      next: (response) => {
        this.status.set('success');
        this.message.set(response.message || 'Email verified successfully!');
      },
      error: (err) => {
        this.status.set('error');
        this.message.set(err.error?.error || 'Verification failed. The link may have expired.');
      }
    });
  }
}