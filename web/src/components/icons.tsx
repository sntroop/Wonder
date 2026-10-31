import type { SVGProps } from "react";

/**
 * Иконки — точные векторы из Android-приложения (res/drawable/ic_*.xml),
 * сконвертированы в SVG 1:1 по pathData. Ничего не рисовалось руками.
 */
type P = SVGProps<SVGSVGElement>;


export const HomeIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M240-200h120v-200q0-17 11.5-28.5T400-440h160q17 0 28.5 11.5T600-400v200h120v-360L480-740 240-560v360Zm-80 0v-360q0-19 8.5-36t23.5-28l240-180q21-16 48-16t48 16l240 180q15 11 23.5 28t8.5 36v360q0 33-23.5 56.5T720-120H560q-17 0-28.5-11.5T520-160v-200h-80v200q0 17-11.5 28.5T400-120H240q-33 0-56.5-23.5T160-200Zm320-270Z" fill="currentColor"/></g>
  </svg>
);


export const SearchIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M380-320q-109 0-184.5-75.5T120-580q0-109 75.5-184.5T380-840q109 0 184.5 75.5T640-580q0 44-14 83t-38 69l224 224q11 11 11 28t-11 28q-11 11-28 11t-28-11L532-372q-30 24-69 38t-83 14Zm0-80q75 0 127.5-52.5T560-580q0-75-52.5-127.5T380-760q-75 0-127.5 52.5T200-580q0 75 52.5 127.5T380-400Z" fill="currentColor"/></g>
  </svg>
);


export const ShortsIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="m411-320 223-143q9-6 9-17t-9-17L411-640q-10-7-20.5-1T380-623v286q0 12 10.5 18t20.5-1ZM160-160q-33 0-56.5-23.5T80-240v-480q0-33 23.5-56.5T160-800h640q33 0 56.5 23.5T880-720v480q0 33-23.5 56.5T800-160H160Z" fill="currentColor"/></g>
  </svg>
);


export const WaveIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M280-280v-400q0-17 11.5-28.5T320-720q17 0 28.5 11.5T360-680v400q0 17-11.5 28.5T320-240q-17 0-28.5-11.5T280-280Zm160 160v-720q0-17 11.5-28.5T480-880q17 0 28.5 11.5T520-840v720q0 17-11.5 28.5T480-80q-17 0-28.5-11.5T440-120ZM120-440v-80q0-17 11.5-28.5T160-560q17 0 28.5 11.5T200-520v80q0 17-11.5 28.5T160-400q-17 0-28.5-11.5T120-440Zm480 160v-400q0-17 11.5-28.5T640-720q17 0 28.5 11.5T680-680v400q0 17-11.5 28.5T640-240q-17 0-28.5-11.5T600-280Zm160-160v-80q0-17 11.5-28.5T800-560q17 0 28.5 11.5T840-520v80q0 17-11.5 28.5T800-400q-17 0-28.5-11.5T760-440Z" fill="currentColor"/></g>
  </svg>
);


export const LibraryIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M500-360q42 0 71-29t29-71v-220h80q17 0 28.5-11.5T720-720q0-17-11.5-28.5T680-760h-80q-17 0-28.5 11.5T560-720v180q-13-10-28-15t-32-5q-42 0-71 29t-29 71q0 42 29 71t71 29ZM320-240q-33 0-56.5-23.5T240-320v-480q0-33 23.5-56.5T320-880h480q33 0 56.5 23.5T880-800v480q0 33-23.5 56.5T800-240H320Zm0-80h480v-480H320v480ZM160-80q-33 0-56.5-23.5T80-160v-520q0-17 11.5-28.5T120-720q17 0 28.5 11.5T160-680v520h520q17 0 28.5 11.5T720-120q0 17-11.5 28.5T680-80H160Zm160-720v480-480Z" fill="currentColor"/></g>
  </svg>
);


export const SettingsIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M433-80q-27 0-46.5-18T363-142l-9-66q-13-5-24.5-12T307-235l-62 26q-25 11-50 2t-39-32l-47-82q-14-23-8-49t27-43l53-40q-1-7-1-13.5v-27q0-6.5 1-13.5l-53-40q-21-17-27-43t8-49l47-82q14-23 39-32t50 2l62 26q11-8 23-15t24-12l9-66q4-26 23.5-44t46.5-18h94q27 0 46.5 18t23.5 44l9 66q13 5 24.5 12t22.5 15l62-26q25-11 50-2t39 32l47 82q14 23 8 49t-27 43l-53 40q1 7 1 13.5v27q0 6.5-2 13.5l53 40q21 17 27 43t-8 49l-48 82q-14 23-39 32t-50-2l-60-26q-11 8-23 15t-24 12l-9 66q-4 26-23.5 44T527-80h-94Zm7-80h79l14-106q31-8 57.5-23.5T639-327l99 41 39-68-86-65q5-14 7-29.5t2-31.5q0-16-2-31.5t-7-29.5l86-65-39-68-99 42q-22-23-48.5-38.5T533-694l-13-106h-79l-14 106q-31 8-57.5 23.5T321-633l-99-41-39 68 86 64q-5 15-7 30t-2 32q0 16 2 31t7 30l-86 65 39 68 99-42q22 23 48.5 38.5T427-266l13 106Zm42-180q58 0 99-41t41-99q0-58-41-99t-99-41q-59 0-99.5 41T342-480q0 58 40.5 99t99.5 41Zm-2-140Z" fill="currentColor"/></g>
  </svg>
);


export const PlayIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M320-273v-414q0-17 12-28.5t28-11.5q5 0 10.5 1.5T381-721l326 207q9 6 13.5 15t4.5 19q0 10-4.5 19T707-446L381-239q-5 3-10.5 4.5T360-233q-16 0-28-11.5T320-273Z" fill="currentColor"/></g>
  </svg>
);


export const PauseIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M640-200q-33 0-56.5-23.5T560-280v-400q0-33 23.5-56.5T640-760q33 0 56.5 23.5T720-680v400q0 33-23.5 56.5T640-200Zm-320 0q-33 0-56.5-23.5T240-280v-400q0-33 23.5-56.5T320-760q33 0 56.5 23.5T400-680v400q0 33-23.5 56.5T320-200Z" fill="currentColor"/></g>
  </svg>
);


export const NextIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M660-280v-400q0-17 11.5-28.5T700-720q17 0 28.5 11.5T740-680v400q0 17-11.5 28.5T700-240q-17 0-28.5-11.5T660-280Zm-440-35v-330q0-18 12-29t28-11q5 0 11 1t11 5l248 166q9 6 13.5 14.5T548-480q0 10-4.5 18.5T530-447L282-281q-5 4-11 5t-11 1q-16 0-28-11t-12-29Z" fill="currentColor"/></g>
  </svg>
);


export const PrevIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M220-280v-400q0-17 11.5-28.5T260-720q17 0 28.5 11.5T300-680v400q0 17-11.5 28.5T260-240q-17 0-28.5-11.5T220-280Zm458-1L430-447q-9-6-13.5-14.5T412-480q0-10 4.5-18.5T430-513l248-166q5-4 11-5t11-1q16 0 28 11t12 29v330q0 18-12 29t-28 11q-5 0-11-1t-11-5Z" fill="currentColor"/></g>
  </svg>
);


export const PlusIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M440-440H240q-17 0-28.5-11.5T200-480q0-17 11.5-28.5T240-520h200v-200q0-17 11.5-28.5T480-760q17 0 28.5 11.5T520-720v200h200q17 0 28.5 11.5T760-480q0 17-11.5 28.5T720-440H520v200q0 17-11.5 28.5T480-200q-17 0-28.5-11.5T440-240v-200Z" fill="currentColor"/></g>
  </svg>
);


export const CloseIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M480-424 284-228q-11 11-28 11t-28-11q-11-11-11-28t11-28l196-196-196-196q-11-11-11-28t11-28q11-11 28-11t28 11l196 196 196-196q11-11 28-11t28 11q11 11 11 28t-11 28L536-480l196 196q11 11 11 28t-11 28q-11 11-28 11t-28-11L480-424Z" fill="currentColor"/></g>
  </svg>
);


export const HeartIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M480-147q-14 0-28.5-5T426-168l-69-63q-106-97-191.5-192.5T80-634q0-94 63-157t157-63q53 0 100 22.5t80 61.5q33-39 80-61.5T660-854q94 0 157 63t63 157q0 115-85 211T602-230l-68 62q-11 11-25.5 16t-28.5 5Z" fill="currentColor"/></g>
  </svg>
);


export const MoreIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M480-160q-33 0-56.5-23.5T400-240q0-33 23.5-56.5T480-320q33 0 56.5 23.5T560-240q0 33-23.5 56.5T480-160Zm0-240q-33 0-56.5-23.5T400-480q0-33 23.5-56.5T480-560q33 0 56.5 23.5T560-480q0 33-23.5 56.5T480-400Zm0-240q-33 0-56.5-23.5T400-720q0-33 23.5-56.5T480-800q33 0 56.5 23.5T560-720q0 33-23.5 56.5T480-640Z" fill="currentColor"/></g>
  </svg>
);


export const DownloadIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M480-337q-8 0-15-2.5t-13-8.5L308-492q-12-12-11.5-28t11.5-28q12-12 28.5-12.5T365-549l75 75v-286q0-17 11.5-28.5T480-800q17 0 28.5 11.5T520-760v286l75-75q12-12 28.5-11.5T652-548q11 12 11.5 28T652-492L508-348q-6 6-13 8.5t-15 2.5ZM240-160q-33 0-56.5-23.5T160-240v-80q0-17 11.5-28.5T200-360q17 0 28.5 11.5T240-320v80h480v-80q0-17 11.5-28.5T760-360q17 0 28.5 11.5T800-320v80q0 33-23.5 56.5T720-160H240Z" fill="currentColor"/></g>
  </svg>
);


export const ShareIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M720-80q-50 0-85-35t-35-85q0-7 1-14.5t3-13.5L322-392q-17 15-38 23.5t-44 8.5q-50 0-85-35t-35-85q0-50 35-85t85-35q23 0 44 8.5t38 23.5l282-164q-2-6-3-13.5t-1-14.5q0-50 35-85t85-35q50 0 85 35t35 85q0 50-35 85t-85 35q-23 0-44-8.5T638-672L356-508q2 6 3 13.5t1 14.5q0 7-1 14.5t-3 13.5l282 164q17-15 38-23.5t44-8.5q50 0 85 35t35 85q0 50-35 85t-85 35Zm0-640q17 0 28.5-11.5T760-760q0-17-11.5-28.5T720-800q-17 0-28.5 11.5T680-760q0 17 11.5 28.5T720-720ZM240-440q17 0 28.5-11.5T280-480q0-17-11.5-28.5T240-520q-17 0-28.5 11.5T200-480q0 17 11.5 28.5T240-440Zm480 280q17 0 28.5-11.5T760-200q0-17-11.5-28.5T720-240q-17 0-28.5 11.5T680-200q0 17 11.5 28.5T720-160Zm0-600ZM240-480Zm480 280Z" fill="currentColor"/></g>
  </svg>
);


export const RemixIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="m274-200 34 34q12 12 11.5 28T308-110q-12 12-28.5 12.5T251-109L148-212q-6-6-8.5-13t-2.5-15q0-8 2.5-15t8.5-13l103-103q12-12 28.5-11.5T308-370q11 12 11.5 28T308-314l-34 34h406v-120q0-17 11.5-28.5T720-440q17 0 28.5 11.5T760-400v120q0 33-23.5 56.5T680-200H274Zm412-480H280v120q0 17-11.5 28.5T240-520q-17 0-28.5-11.5T200-560v-120q0-33 23.5-56.5T280-760h406l-34-34q-12-12-11.5-28t11.5-28q12-12 28.5-12.5T709-851l103 103q6 6 8.5 13t2.5 15q0 8-2.5 15t-8.5 13L709-589q-12 12-28.5 11.5T652-590q-11-12-11.5-28t11.5-28l34-34Z" fill="currentColor"/></g>
  </svg>
);


export const TextIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M160-320v-480 480Zm440 80H240l-92 92q-19 19-43.5 8.5T80-177v-623q0-33 23.5-56.5T160-880h440q33 0 56.5 23.5T680-800v40q0 17-11.5 28.5T640-720q-17 0-28.5-11.5T600-760v-40H160v480h440v-120q0-17 11.5-28.5T640-480q17 0 28.5 11.5T680-440v120q0 33-23.5 56.5T600-240ZM280-400h80q17 0 28.5-11.5T400-440q0-17-11.5-28.5T360-480h-80q-17 0-28.5 11.5T240-440q0 17 11.5 28.5T280-400Zm480-80q-50 0-85-35t-35-85q0-50 35-85t85-35q11 0 21 2t19 5v-167q0-17 11.5-28.5T840-920h80q17 0 28.5 11.5T960-880q0 17-11.5 28.5T920-840h-40v240q0 50-35 85t-85 35Zm-480-40h200q17 0 28.5-11.5T520-560q0-17-11.5-28.5T480-600H280q-17 0-28.5 11.5T240-560q0 17 11.5 28.5T280-520Zm0-120h200q17 0 28.5-11.5T520-680q0-17-11.5-28.5T480-720H280q-17 0-28.5 11.5T240-680q0 17 11.5 28.5T280-640Z" fill="currentColor"/></g>
  </svg>
);


export const NoteIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M400-120q-66 0-113-47t-47-113q0-66 47-113t113-47q23 0 42.5 5.5T480-418v-382q0-17 11.5-28.5T520-840h160q17 0 28.5 11.5T720-800v80q0 17-11.5 28.5T680-680H560v400q0 66-47 113t-113 47Z" fill="currentColor"/></g>
  </svg>
);


export const PlaylistIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M640-160q-50 0-85-35t-35-85q0-50 35-85t85-35q11 0 21 1.5t19 6.5v-288q0-17 11.5-28.5T720-720h120q17 0 28.5 11.5T880-680q0 17-11.5 28.5T840-640h-80v360q0 50-35 85t-85 35ZM160-320q-17 0-28.5-11.5T120-360q0-17 11.5-28.5T160-400h240q17 0 28.5 11.5T440-360q0 17-11.5 28.5T400-320H160Zm0-160q-17 0-28.5-11.5T120-520q0-17 11.5-28.5T160-560h400q17 0 28.5 11.5T600-520q0 17-11.5 28.5T560-480H160Zm0-160q-17 0-28.5-11.5T120-680q0-17 11.5-28.5T160-720h400q17 0 28.5 11.5T600-680q0 17-11.5 28.5T560-640H160Z" fill="currentColor"/></g>
  </svg>
);


export const CommentIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="m240-240-92 92q-19 19-43.5 8.5T80-177v-623q0-33 23.5-56.5T160-880h640q33 0 56.5 23.5T880-800v480q0 33-23.5 56.5T800-240H240Z" fill="currentColor"/></g>
  </svg>
);


export const CheckIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="m382-354 339-339q12-12 28-12t28 12q12 12 12 28.5T777-636L410-268q-12 12-28 12t-28-12L182-440q-12-12-11.5-28.5T183-497q12-12 28.5-12t28.5 12l142 143Z" fill="currentColor"/></g>
  </svg>
);


export const TrashIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M280-120q-33 0-56.5-23.5T200-200v-520q-17 0-28.5-11.5T160-760q0-17 11.5-28.5T200-800h160q0-17 11.5-28.5T400-840h160q17 0 28.5 11.5T600-800h160q17 0 28.5 11.5T800-760q0 17-11.5 28.5T760-720v520q0 33-23.5 56.5T680-120H280Zm400-600H280v520h400v-520ZM400-280q17 0 28.5-11.5T440-320v-280q0-17-11.5-28.5T400-640q-17 0-28.5 11.5T360-600v280q0 17 11.5 28.5T400-280Zm160 0q17 0 28.5-11.5T600-320v-280q0-17-11.5-28.5T560-640q-17 0-28.5 11.5T520-600v280q0 17 11.5 28.5T560-280ZM280-720v520-520Z" fill="currentColor"/></g>
  </svg>
);


export const ArtistIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M700-160q-42 0-71-29t-29-71q0-42 29-71t71-29q8 0 18 1.5t22 6.5v-168q0-17 11.5-28.5T780-560h60q17 0 28.5 11.5T880-520q0 17-11.5 28.5T840-480h-40v220q0 42-29 71t-71 29Zm-540 0q-17 0-28.5-11.5T120-200v-72q0-35 17.5-63t46.5-43q62-31 126-46.5T440-440q23 0 47 1.5t47 6.5q21 4 29 18t6 29q-2 15-14 26t-31 7q-21-4-41.5-6t-42.5-2q-57 0-112 14t-108 40q-9 5-14.5 14t-5.5 20v32h293q20 0 30 12.5t10 27.5q0 15-10 27.5T493-160H160Zm280-320q-66 0-113-47t-47-113q0-66 47-113t113-47q66 0 113 47t47 113q0 66-47 113t-113 47Zm0-80q33 0 56.5-23.5T520-640q0-33-23.5-56.5T440-720q-33 0-56.5 23.5T360-640q0 33 23.5 56.5T440-560Zm0-80Zm0 400Z" fill="currentColor"/></g>
  </svg>
);


export const BackIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="m313-440 196 196q12 12 11.5 28T508-188q-12 11-28 11.5T452-188L188-452q-6-6-8.5-13t-2.5-15q0-8 2.5-15t8.5-13l264-264q11-11 27.5-11t28.5 11q12 12 12 28.5T508-715L313-520h447q17 0 28.5 11.5T800-480q0 17-11.5 28.5T760-440H313Z" fill="currentColor"/></g>
  </svg>
);


export const ChevronRightIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M504-480 348-636q-11-11-11-28t11-28q11-11 28-11t28 11l184 184q6 6 8.5 13t2.5 15q0 8-2.5 15t-8.5 13L404-268q-11 11-28 11t-28-11q-11-11-11-28t11-28l156-156Z" fill="currentColor"/></g>
  </svg>
);


export const ChevronDownIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(-0.0 960.0) scale(1.0 1.0)"><path d="M480-362q-8 0-15-2.5t-13-8.5L268-557q-11-11-11-28t11-28q11-11 28-11t28 11l156 156 156-156q11-11 28-11t28 11q11 11 11 28t-11 28L508-373q-6 6-13 8.5t-15 2.5Z" fill="currentColor"/></g>
  </svg>
);


export const ChevronLeft = (p: P) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" aria-hidden {...p}>
    <path d="m14.5 5-7 7 7 7" />
  </svg>
);

export const RefreshIcon = (p: P) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" aria-hidden {...p}>
    <path d="M20 11a8 8 0 1 0-2.3 5.7" />
    <path d="M20 4v7h-7" />
  </svg>
);

export const VolumeIcon = (p: P) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" aria-hidden {...p}>
    <path d="M4 9.5h3L12 5v14l-5-4.5H4z" />
    <path d="M16 8.5a5 5 0 0 1 0 7" />
  </svg>
);

export const MuteIcon = (p: P) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" aria-hidden {...p}>
    <path d="M4 9.5h3L12 5v14l-5-4.5H4z" />
    <path d="m16.5 9.5 5 5m0-5-5 5" />
  </svg>
);

export const QueueIcon = (p: P) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" aria-hidden {...p}>
    <path d="M4 6h16M4 12h10M4 18h7" />
    <circle cx="18" cy="17" r="3" />
    <path d="M21 17v-6l-3 1" />
  </svg>
);

export const UserIcon = (p: P) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" aria-hidden {...p}>
    <circle cx="12" cy="8" r="4" />
    <path d="M4.5 21c1.2-4 4-6 7.5-6s6.3 2 7.5 6" />
  </svg>
);

export const SendIcon = (p: P) => (
  <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden {...p}>
    <path d="M21.6 3.4 2.9 10.6c-1 .4-1 1.8.1 2.1l4.6 1.4 1.8 5.5c.3.9 1.4 1.1 2 .4l2.5-2.7 4.6 3.4c.7.5 1.7.1 1.9-.7l3.4-14.9c.2-1-.8-1.9-2.2-1.7Z" />
  </svg>
);

export const DislikeIcon = (p: P) => (
  <svg viewBox="0 0 960 960" fill="currentColor" aria-hidden {...p}>
    <g transform="translate(0 960)">
      <path d="M120-320q-32 0-56-24t-24-56v-80q0-7 2-15t4-15l120-282q9-20 30-34t44-14h440v520L440-82q-15 15-35.5 17.5T365-72q-19-10-28-28t-4-37l45-183H120Zm480-34v-406H240L120-480v80h360l-54 220 174-174Zm200-486q33 0 56.5 23.5T880-760v360q0 33-23.5 56.5T800-320H680v-80h120v-360H680v-80h120Zm-200 80v406-406Z" />
    </g>
  </svg>
);

export const FxIcon = (p: P) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" aria-hidden {...p}>
    <path d="M4 6h6M14 6h6M4 12h10M18 12h2M4 18h2M10 18h10" />
    <circle cx="12" cy="6" r="2" />
    <circle cx="16" cy="12" r="2" />
    <circle cx="8" cy="18" r="2" />
  </svg>
);

export const PaintIcon = (p: P) => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" aria-hidden {...p}>
    <path d="M12 3a9 9 0 1 0 0 18h1.5a2 2 0 0 0 0-4H13a1.5 1.5 0 0 1 0-3h3a5 5 0 0 0 5-5c0-3.3-4-6-9-6Z" />
    <circle cx="7.5" cy="10" r="1" fill="currentColor" stroke="none" />
    <circle cx="9.5" cy="6.5" r="1" fill="currentColor" stroke="none" />
    <circle cx="14" cy="6" r="1" fill="currentColor" stroke="none" />
  </svg>
);
