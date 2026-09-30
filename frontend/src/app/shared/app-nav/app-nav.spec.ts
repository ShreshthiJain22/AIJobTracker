import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AppNav } from './app-nav';

describe('AppNav', () => {
  let component: AppNav;
  let fixture: ComponentFixture<AppNav>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppNav],
    }).compileComponents();

    fixture = TestBed.createComponent(AppNav);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
