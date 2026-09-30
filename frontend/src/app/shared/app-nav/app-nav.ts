import { Component, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from '../../core/services/auth';
import { UserSettingsService } from '../../core/services/user-settings';
import { GoalsSetup } from '../../features/onboarding/goals-setup/goals-setup';
import { ColumnsSetup } from '../../features/settings/columns-setup/columns-setup';

@Component({
  selector: 'app-nav',
  imports: [MatButtonModule, MatIconModule, MatMenuModule, MatTooltipModule],
  templateUrl: './app-nav.html',
  styleUrl: './app-nav.scss'
})
export class AppNav implements OnInit {
  avatarPath = signal('avatars/avatar-female.svg');

  constructor(
    private authService: AuthService,
    private userSettingsService: UserSettingsService,
    private router: Router,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.userSettingsService.getMySettings().subscribe({
      next: (settings) => {
        const gender = settings?.gender || 'female';
        this.avatarPath.set(`avatars/avatar-${gender}.svg`);
      },
      error: () => {}
    });
  }

  goToGoals(): void {
    const dialogRef = this.dialog.open(GoalsSetup, {
      width: '500px',
      maxWidth: '90vw',
      maxHeight: '80vh',
      panelClass: 'goals-dialog',
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe((saved) => {
      if (saved) {
        window.location.reload();
      }
    });
  }

  goToColumns(): void {
    const dialogRef = this.dialog.open(ColumnsSetup, {
      width: '420px',
      maxWidth: '90vw',
      maxHeight: '80vh',
      panelClass: 'columns-dialog',
      autoFocus: false
    });

    dialogRef.afterClosed().subscribe((saved) => {
      if (saved) {
        window.location.reload();
      }
    });
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}