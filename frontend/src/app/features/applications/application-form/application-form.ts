import { Component, Inject, Optional, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { Application } from '../../../core/models/application';
import { NotificationService } from '../../../core/services/notification';
import { UserSettingsService } from '../../../core/services/user-settings';

@Component({
  selector: 'app-application-form',
  imports: [CommonModule, FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSelectModule, MatSlideToggleModule],
  templateUrl: './application-form.html',
  styleUrl: './application-form.scss'
})
export class ApplicationForm implements OnInit {
  application: Application;
  statusOptions = ['Applied', 'Referral Requested', 'Interview', 'Offer', 'Rejected'];
  maxDate = new Date().toISOString().split('T')[0];
  visibleFields: string[] = ['jdLink', 'source', 'emailUsed', 'contactPerson', 'referralRequested'];
  errorMessage = signal('');

  constructor(
    private dialogRef: MatDialogRef<ApplicationForm>,
    private notification: NotificationService,
    private userSettingsService: UserSettingsService,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { application: Application } | null
  ) {
    this.application = this.data?.application
      ? { ...this.data.application }
      : {
          company: '',
          role: '',
          status: 'Applied',
          appliedDate: '',
          referralRequested: false,
          referralReceived: false
        };
  }

  ngOnInit(): void {
    this.userSettingsService.getMySettings().subscribe({
      next: (settings) => {
        if (settings?.visibleFields) {
          this.visibleFields = settings.visibleFields;
        }
      },
      error: () => {}
    });
  }

  showField(key: string): boolean {
    return this.visibleFields.includes(key);
  }

  onSave(): void {
    this.errorMessage.set('');

    const company = this.application.company?.trim();
    const role = this.application.role?.trim();

    if (!company || !role) {
      this.errorMessage.set('Company and Role are required.');
      return;
    }

    this.application.company = company;
    this.application.role = role;

    if (!this.application.appliedDate) {
      this.errorMessage.set('Applied Date is required.');
      return;
    }

    const today = new Date().toISOString().split('T')[0];
    if (this.application.appliedDate > today) {
      this.errorMessage.set('Applied Date cannot be in the future.');
      return;
    }

    if (this.application.jdLink) {
      const urlPattern = /^https?:\/\/.+\..+/;
      if (!urlPattern.test(this.application.jdLink)) {
        this.errorMessage.set('JD Link must be a valid URL starting with http:// or https://');
        return;
      }
    }

    this.dialogRef.close(this.application);
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}