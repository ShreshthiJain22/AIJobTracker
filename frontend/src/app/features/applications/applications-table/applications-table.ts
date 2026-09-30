import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { ApplicationService } from '../../../core/services/application';
import { NotificationService } from '../../../core/services/notification';
import { AuthService } from '../../../core/services/auth';
import { UserSettingsService } from '../../../core/services/user-settings';
import { Application } from '../../../core/models/application';
import { ApplicationForm } from '../application-form/application-form';
import { ApplicationDetail } from '../application-detail/application-detail';
import { AppNav } from '../../../shared/app-nav/app-nav';
import { MatMenuModule } from '@angular/material/menu';

@Component({
  selector: 'app-applications-table',
  imports: [CommonModule, MatTableModule, MatButtonModule, MatIconModule, MatDialogModule, AppNav,MatMenuModule],
  templateUrl: './applications-table.html',
  styleUrl: './applications-table.scss'
})
export class ApplicationsTable implements OnInit {
  applications = signal<Application[]>([]);
  quoteOfTheDay = signal('Loading today\'s motivation...');
  displayedColumns = signal<string[]>(['company', 'role', 'status', 'appliedDate', 'actions']);

  constructor(
    private applicationService: ApplicationService,
    private notification: NotificationService,
    private authService: AuthService,
    private userSettingsService: UserSettingsService,
    private router: Router,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.loadApplications();
    this.loadQuote();
    this.loadColumnPreferences();
  }

  loadColumnPreferences(): void {
    this.userSettingsService.getMySettings().subscribe({
      next: (settings) => {
        const visible = settings?.visibleFields || ['jdLink', 'source', 'emailUsed', 'contactPerson', 'referralRequested'];
        const coreColumns = ['company', 'role', 'status', 'appliedDate'];
        this.displayedColumns.set([...coreColumns, ...visible, 'actions']);
      },
      error: () => {
        this.displayedColumns.set(['company', 'role', 'status', 'appliedDate', 'jdLink', 'source', 'emailUsed', 'contactPerson', 'referralRequested', 'actions']);
      }
    });
  }

  loadApplications(): void {
    this.applicationService.getAllApplications().subscribe({
      next: (data) => this.applications.set(data),
      error: () => this.notification.error('Failed to load applications.')
    });
  }

  loadQuote(): void {
    this.applicationService.getQuoteOfTheDay().subscribe({
      next: (quote) => this.quoteOfTheDay.set(quote),
      error: () => this.quoteOfTheDay.set('You\'ve got this. Keep applying!')
    });
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  openAddDialog(): void {
    const dialogRef = this.dialog.open(ApplicationForm, {
      width: '480px',
      maxWidth: '90vw'
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.applicationService.createApplication(result).subscribe({
          next: () => {
            this.loadApplications();
            this.notification.success('Application added!');
          },
          error: () => this.notification.error('Failed to add application.')
        });
      }
    });
  }

  onEdit(app: Application): void {
    const dialogRef = this.dialog.open(ApplicationForm, {
      width: '480px',
      maxWidth: '90vw',
      data: { application: app }
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result && app.id) {
        this.applicationService.updateApplication(app.id, result).subscribe({
          next: () => {
            this.loadApplications();
            this.notification.success('Application updated!');
          },
          error: () => this.notification.error('Failed to update application.')
        });
      }
    });
  }

  async onDelete(app: Application): Promise<void> {
    if (!app.id) return;
    const confirmed = await this.notification.confirmDelete(app.company);
    if (!confirmed) return;

    this.applicationService.deleteApplication(app.id).subscribe({
      next: () => {
        this.loadApplications();
        this.notification.success('Application deleted.');
      },
      error: () => this.notification.error('Failed to delete application.')
    });
  }

  onView(app: Application): void {
    this.dialog.open(ApplicationDetail, {
      data: { application: app },
      position: { top: '0', right: '0' },
      height: '100vh',
      width: '380px',
      maxWidth: '100vw',
      panelClass: 'side-panel-dialog',
      restoreFocus: false
    });
  }

  openJdLink(url: string): void {
    window.open(url, '_blank', 'noopener,noreferrer');
  }
}