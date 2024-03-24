import { Component, OnInit } from '@angular/core';
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { NgIf } from "@angular/common";
import {
  TrCopyLibraryToLibraryComponent
} from "app/trainer-road/tr-copy-library-to-library/tr-copy-library-to-library.component";
import { MatExpansionModule } from "@angular/material/expansion";
import {
  TpCopyCalendarToCalendarComponent
} from "app/training-peaks/tp-copy-calendar-to-calendar/tp-copy-calendar-to-calendar.component";
import {
  TpCopyCalendarToLibraryComponent
} from "app/training-peaks/tp-copy-calendar-to-library/tp-copy-calendar-to-library.component";
import {
  TpCopyLibraryContainerComponent
} from "app/training-peaks/tp-copy-library-container/tp-copy-library-container.component";
import {
  TrCopyCalendarToLibraryComponent
} from "app/trainer-road/tr-copy-calendar-to-library/tr-copy-calendar-to-library.component";
import { Platform } from "infrastructure/platform";
import { ConfigurationClient } from "infrastructure/configuration.client";
import { ProgressBarService } from "infrastructure/progress-bar.service";

@Component({
  selector: 'app-trainer-road',
  standalone: true,
  imports: [
    TrCopyLibraryToLibraryComponent,
    MatExpansionModule,
    TpCopyCalendarToCalendarComponent,
    TpCopyCalendarToLibraryComponent,
    TpCopyLibraryContainerComponent,
    TrCopyCalendarToLibraryComponent,
    NgIf,
    MatProgressBarModule
  ],
  templateUrl: './trainer-road.component.html',
  styleUrl: './trainer-road.component.scss'
})
export class TrainerRoadComponent implements OnInit {
  platformValid: any = undefined;

  private readonly platform = Platform.TRAINER_ROAD

  constructor(
    private configurationClient: ConfigurationClient,
    private progressBarService: ProgressBarService
  ) {
  }

  ngOnInit(): void {
    this.progressBarService.start()
    this.configurationClient.isValid(this.platform.key).subscribe(value => {
      this.platformValid = value
      this.progressBarService.stop()
    })
  }
}
