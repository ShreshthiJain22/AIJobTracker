import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GoalsSetup } from './goals-setup';

describe('GoalsSetup', () => {
  let component: GoalsSetup;
  let fixture: ComponentFixture<GoalsSetup>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GoalsSetup],
    }).compileComponents();

    fixture = TestBed.createComponent(GoalsSetup);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
