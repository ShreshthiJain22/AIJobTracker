import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ColumnsSetup } from './columns-setup';

describe('ColumnsSetup', () => {
  let component: ColumnsSetup;
  let fixture: ComponentFixture<ColumnsSetup>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ColumnsSetup],
    }).compileComponents();

    fixture = TestBed.createComponent(ColumnsSetup);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
