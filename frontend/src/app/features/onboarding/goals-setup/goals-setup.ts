import { Component, OnInit, signal, Optional } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UserSettingsService } from '../../../core/services/user-settings';
import { NotificationService } from '../../../core/services/notification';
import { OnboardingStateService } from '../../../core/services/onboarding-state';

@Component({
  selector: 'app-goals-setup',
  imports: [CommonModule, FormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSelectModule, MatSlideToggleModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './goals-setup.html',
  styleUrl: './goals-setup.scss'
})
export class GoalsSetup implements OnInit {
  goals = signal<string[]>(['', '']);
  reminderLanguage = signal('Hinglish');
  remindersEnabled = signal(true);
  reminderTime = signal('21:00');
  reminderFrequency = signal('Daily');
  languageOptions = ['English', 'Hindi', 'Hinglish'];
  frequencyOptions = ['Daily', 'Every 3 Days', 'Weekly', 'Monthly'];
  isEditing = signal(false);
  loading = signal(true);
  gender = signal<string>('female');
  constructor(
    private userSettingsService: UserSettingsService,
    private notification: NotificationService,
    private router: Router,
    private onboardingState: OnboardingStateService,
    @Optional() private dialogRef: MatDialogRef<GoalsSetup> | null
  ) {}

  ngOnInit(): void {
    this.userSettingsService.getMySettings().subscribe({
      next: (settings) => {
        if (settings && settings.goals && settings.goals.length > 0) {
          this.goals.set([...settings.goals]);
          this.reminderLanguage.set(settings.reminderLanguage || 'Hinglish');
          this.remindersEnabled.set(settings.remindersEnabled);
          this.reminderTime.set(settings.reminderTime || '21:00');
          this.reminderFrequency.set(settings.reminderFrequency || 'Daily');
          this.gender.set(settings.gender || 'female');
          this.isEditing.set(true);
        }
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
      }
    });
  }

  trackByIndex(index: number): number {
    return index;
  }

  addGoal(): void {
    if (this.goals().length < 5) {
      this.goals.update(current => [...current, '']);
    }
  }

  removeGoal(index: number): void {
    if (this.goals().length > 2) {
      this.goals.update(current => current.filter((_, i) => i !== index));
    }
  }

  updateGoal(index: number, value: string): void {
    this.goals.update(current => {
      const copy = [...current];
      copy[index] = value;
      return copy;
    });
  }
  selectGender(value: string): void {
    this.gender.set(value);
  }
  onSave(): void {
    const filledGoals = this.goals().map(g => g.trim()).filter(g => g.length > 0);
  
    if (filledGoals.length < 2) {
      this.notification.error('Please enter at least 2 goals.');
      return;
    }
  
    const payload: any = {
      goals: filledGoals,
      reminderLanguage: this.reminderLanguage(),
      remindersEnabled: this.remindersEnabled(),
      reminderTime: this.reminderTime(),
      reminderFrequency: this.reminderFrequency(),
      gender: this.gender()
    };
  
    if (this.onboardingState.pendingVisibleFields) {
      payload.visibleFields = this.onboardingState.pendingVisibleFields;
    }
  
    this.userSettingsService.saveMySettings(payload).subscribe({
      next: () => {
        this.onboardingState.pendingVisibleFields = null;
        this.notification.success(this.isEditing() ? 'Goals updated!' : 'Your goals are set! Let\'s get started.');
        if (this.dialogRef) {
          this.dialogRef.close(true);
        } else {
          this.router.navigate(['/']);
        }
      },
      error: () => this.notification.error('Failed to save your goals.')
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