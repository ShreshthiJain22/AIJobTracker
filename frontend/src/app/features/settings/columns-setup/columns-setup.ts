import { Component, OnInit, signal, Optional } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UserSettingsService } from '../../../core/services/user-settings';
import { NotificationService } from '../../../core/services/notification';
import { OnboardingStateService } from '../../../core/services/onboarding-state';

interface FieldOption {
  key: string;
  label: string;
}

@Component({
  selector: 'app-columns-setup',
  imports: [CommonModule, FormsModule, MatDialogModule, MatCheckboxModule, MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './columns-setup.html',
  styleUrl: './columns-setup.scss'
})
export class ColumnsSetup implements OnInit {
  availableFields: FieldOption[] = [
    { key: 'jdLink', label: 'JD Link' },
    { key: 'source', label: 'Source' },
    { key: 'emailUsed', label: 'Email Used' },
    { key: 'contactPerson', label: 'Contact Person' },
    { key: 'referralRequested', label: 'Referral Tracking' }
  ];

  selectedFields = signal<string[]>([]);
  loading = signal(true);
  isOnboarding = signal(false);
  private currentSettings: any = null;

  constructor(
    private userSettingsService: UserSettingsService,
    private notification: NotificationService,
    private router: Router,
    private onboardingState: OnboardingStateService,
    @Optional() private dialogRef: MatDialogRef<ColumnsSetup> | null
  ) {}

  ngOnInit(): void {
    this.userSettingsService.getMySettings().subscribe({
      next: (settings) => {
        this.currentSettings = settings;
        const hasNoGoalsYet = !settings || !settings.goals || settings.goals.length === 0;
        this.isOnboarding.set(!this.dialogRef && hasNoGoalsYet);
        this.selectedFields.set(settings?.visibleFields || this.availableFields.map(f => f.key));
        this.loading.set(false);
      },
      error: () => {
        this.isOnboarding.set(!this.dialogRef);
        this.selectedFields.set(this.availableFields.map(f => f.key));
        this.loading.set(false);
      }
    });
  }

  isChecked(key: string): boolean {
    return this.selectedFields().includes(key);
  }

  toggleField(key: string): void {
    this.selectedFields.update(current =>
      current.includes(key) ? current.filter(f => f !== key) : [...current, key]
    );
  }

  onSave(): void {
    if (this.isOnboarding()) {
      this.onboardingState.pendingVisibleFields = this.selectedFields();
      this.router.navigate(['/goals-setup']);
      return;
    }

    if (!this.currentSettings) {
      this.notification.error('Please set up your goals first.');
      return;
    }

    this.userSettingsService.saveMySettings({
      ...this.currentSettings,
      visibleFields: this.selectedFields()
    }).subscribe({
      next: () => {
        this.notification.success('Columns updated!');
        if (this.dialogRef) {
          this.dialogRef.close(true);
        } else {
          this.router.navigate(['/']);
        }
      },
      error: () => this.notification.error('Failed to save columns.')
    });
  }

  onCancel(): void {
    if (this.dialogRef) {
      this.dialogRef.close();
    } else {
      this.router.navigate(['/']);
    }
  }
}