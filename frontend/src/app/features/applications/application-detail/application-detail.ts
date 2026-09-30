import { Component, Inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { HttpClient } from '@angular/common/http';
import { Application } from '../../../core/models/application';
import { UserSettingsService } from '../../../core/services/user-settings';

@Component({
  selector: 'app-application-detail',
  imports: [CommonModule, MatDialogModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './application-detail.html',
  styleUrl: './application-detail.scss'
})
export class ApplicationDetail implements OnInit {
  private allStages: string[] = ['Applied', 'Interview', 'Offer'];

  captions = signal<string[]>([]);
  loadingCaptions = signal(true);
  reachedStages: string[] = [];

  constructor(
    public dialogRef: MatDialogRef<ApplicationDetail>,
    @Inject(MAT_DIALOG_DATA) public data: { application: Application },
    private http: HttpClient,
    private userSettingsService: UserSettingsService
  ) {}

  ngOnInit(): void {
    this.userSettingsService.getMySettings().subscribe({
      next: (settings) => {
        const tracksReferral = settings?.visibleFields?.includes('referralRequested') ?? false;
        this.allStages = tracksReferral
          ? ['Applied', 'Referral Requested', 'Interview', 'Offer']
          : ['Applied', 'Interview', 'Offer'];
        this.computeReachedStages();
        this.fetchCaptions();
      },
      error: () => {
        this.computeReachedStages();
        this.fetchCaptions();
      }
    });
  }

  private computeReachedStages(): void {
    const status = this.data.application.status;
    const index = this.allStages.indexOf(status);
    const upTo = index >= 0 ? index : 0;
    this.reachedStages = this.allStages.slice(0, upTo + 1);
  }

  private fetchCaptions(): void {
    this.http.get<string[]>(`https://aijobtracker.duckdns.org/api/applications/${this.data.application.id}/stage-captions`)
      .subscribe({
        next: (captions) => {
          this.captions.set(captions);
          this.loadingCaptions.set(false);
        },
        error: () => {
          this.captions.set(this.reachedStages.map(() => 'One step closer to your goal.'));
          this.loadingCaptions.set(false);
        }
      });
  }

  getCaption(stageIndex: number): string {
    const current = this.captions();
    return current[stageIndex] || 'One step closer to your goal.';
  }

  isRejected(): boolean {
    return this.data.application.status === 'Rejected';
  }

  isCurrent(stage: string): boolean {
    return stage === this.reachedStages[this.reachedStages.length - 1];
  }
}