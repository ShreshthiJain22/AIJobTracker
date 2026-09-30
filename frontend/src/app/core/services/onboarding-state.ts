import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class OnboardingStateService {
  pendingVisibleFields: string[] | null = null;
}